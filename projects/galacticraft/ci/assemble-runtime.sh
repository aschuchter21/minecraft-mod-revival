#!/usr/bin/env bash
set -euo pipefail

# Assemble the first runnable Forge-facing Galacticraft artifact from the exact
# recovered 1.20.1 classes plus the source-ported Forge overlay. This script is
# intentionally CI-friendly: it never mutates the recovered upstream checkout.

REVIVAL_ROOT="${1:-Revival}"
UPSTREAM_GC="${2:-Galacticraft}"
SUPPORT_JAR="${3:-gc-1.20.1-support.jar}"
OUT_DIR="${4:-ForgeRuntimeValidation}"
OVERLAY_SRC="$REVIVAL_ROOT/projects/galacticraft/forge-overlays/src/main/java"

MACHINE_BUILD="$REVIVAL_ROOT/projects/machinelib/forge-backport/build/libs"
MACHINE_DEV_JAR="$MACHINE_BUILD/MachineLib-Forge-1.20.1-dev.jar"
MACHINE_RUNTIME_JAR=$(find "$MACHINE_BUILD" -maxdepth 1 -type f \
  -name 'MachineLib-Forge-1.20.1-*.jar' \
  ! -name '*-sources.jar' ! -name '*-dev.jar' | head -n 1)

[[ -s "$SUPPORT_JAR" ]]
[[ -s "$MACHINE_DEV_JAR" ]]
[[ -n "$MACHINE_RUNTIME_JAR" && -s "$MACHINE_RUNTIME_JAR" ]]
[[ -d "$OVERLAY_SRC" ]]

rm -rf "$OUT_DIR"
cp -R "$REVIVAL_ROOT/projects/galacticraft/forge-skeleton" "$OUT_DIR"
mkdir -p "$OUT_DIR/libs" "$OUT_DIR/upstream-classes"

cp "$SUPPORT_JAR" "$OUT_DIR/libs/gc-1.20.1-support.jar"
cp "$MACHINE_DEV_JAR" "$OUT_DIR/libs/machinelib-forge-dev.jar"
cp "$MACHINE_RUNTIME_JAR" "$OUT_DIR/libs/machinelib-forge-runtime.jar"

# Forge-port sources are compiled as the primary implementation.
cp -R "$OVERLAY_SRC/." "$OUT_DIR/src/main/java/"

# The support jar contains the recovered upstream implementation. Extract it so
# loader-neutral classes remain available to the Forge runtime.
(
  cd "$OUT_DIR/upstream-classes"
  jar xf ../libs/gc-1.20.1-support.jar
)

# IMPORTANT: never leave two definitions of a source-ported class on the runtime
# classpath. Remove every recovered class that has a Forge overlay source,
# including compiler-generated nested/anonymous classes. The freshly compiled
# overlay is then the sole runtime definition.
REMOVED_OVERLAY_CLASSES=0
while IFS= read -r source; do
  rel="${source#"$OVERLAY_SRC/"}"
  stem="${rel%.java}"
  class_file="$OUT_DIR/upstream-classes/$stem.class"
  class_dir=$(dirname "$class_file")
  class_base=$(basename "$stem")

  if [[ -f "$class_file" ]]; then
    rm -f "$class_file"
    REMOVED_OVERLAY_CLASSES=$((REMOVED_OVERLAY_CLASSES + 1))
  fi

  if [[ -d "$class_dir" ]]; then
    while IFS= read -r nested; do
      rm -f "$nested"
      REMOVED_OVERLAY_CLASSES=$((REMOVED_OVERLAY_CLASSES + 1))
    done < <(find "$class_dir" -maxdepth 1 -type f -name "${class_base}\$*.class" -print)
  fi
done < <(find "$OVERLAY_SRC" -type f -name '*.java' -print | sort)

echo "Removed $REMOVED_OVERLAY_CLASSES recovered class files superseded by Forge overlays."

# Fail assembly immediately if any recovered duplicate survives the pruning pass.
while IFS= read -r source; do
  rel="${source#"$OVERLAY_SRC/"}"
  stem="${rel%.java}"
  class_file="$OUT_DIR/upstream-classes/$stem.class"
  class_dir=$(dirname "$class_file")
  class_base=$(basename "$stem")

  if [[ -f "$class_file" ]]; then
    echo "Recovered duplicate survived for Forge overlay: $rel" >&2
    exit 1
  fi
  if [[ -d "$class_dir" ]] && find "$class_dir" -maxdepth 1 -type f -name "${class_base}\$*.class" -print -quit | grep -q .; then
    echo "Recovered nested duplicate survived for Forge overlay: $rel" >&2
    exit 1
  fi
done < <(find "$OVERLAY_SRC" -type f -name '*.java' -print | sort)

# Bring over only loader-neutral game resources for the first runtime test.
# Fabric metadata, access widener and Fabric mixin configs are deliberately not
# copied; those subsystems are being ported independently.
cp -R "$UPSTREAM_GC/src/main/resources/assets" "$OUT_DIR/src/main/resources/"
cp -R "$UPSTREAM_GC/src/main/resources/data" "$OUT_DIR/src/main/resources/"
cp "$UPSTREAM_GC/src/main/resources/pack.mcmeta" "$OUT_DIR/src/main/resources/pack.mcmeta"

cat >> "$OUT_DIR/build.gradle" <<'GRADLE'

// The recovered upstream classes must live in Forge's normal Java class output.
// Adding them as a separate SourceSet output lets them bypass the production
// remapping/reobfuscation path. Copy them after the Forge overlays compile so
// LegacyForge sees one coherent main class tree before jar/reobf processing.
def recoveredUpstreamClasses = file('upstream-classes')

tasks.named('compileJava') {
    inputs.dir(recoveredUpstreamClasses)
    doLast {
        project.copy {
            from recoveredUpstreamClasses
            into destinationDirectory.get().asFile
            include '**/*.class'
        }
    }
}

dependencies {
    compileOnly files('libs/gc-1.20.1-support.jar', 'libs/machinelib-forge-dev.jar')
    runtimeOnly files('libs/machinelib-forge-runtime.jar')
}

tasks.named('jar') {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
GRADLE

echo '--- Runtime overlay sources ---'
find "$OUT_DIR/src/main/java" -type f -name '*.java' -printf '%P\n' | sort

echo '--- Runtime libraries ---'
find "$OUT_DIR/libs" -maxdepth 1 -type f -printf '%f\n' | sort

gradle -p "$OUT_DIR" clean build --stacktrace

RUNTIME_JAR=$(find "$OUT_DIR/build/libs" -maxdepth 1 -type f \
  -name 'Galacticraft-Forge-1.20.1-*.jar' ! -name '*-sources.jar' | head -n 1)
[[ -n "$RUNTIME_JAR" && -s "$RUNTIME_JAR" ]]

# Recovered classes should now be physically staged beside compiled overlays,
# rather than represented as an auxiliary SourceSet output directory.
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/GCBlocks.class" ]]
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/block/entity/RocketWorkbenchBlockEntity.class" ]]

echo "Experimental Galacticraft runtime: $RUNTIME_JAR"
echo "MachineLib runtime: $MACHINE_RUNTIME_JAR"

# Snapshot the listing once. Besides making debugging easier, this avoids
# pipefail/SIGPIPE false negatives from repeatedly piping `jar tf` into grep -q.
JAR_ENTRIES="$OUT_DIR/runtime-jar-entries.txt"
jar tf "$RUNTIME_JAR" > "$JAR_ENTRIES"

# Assert that this is a real assembled runtime rather than the old skeleton jar.
for entry in \
  'META-INF/mods.toml' \
  'META-INF/accesstransformer.cfg' \
  'dev/galacticraft/forge/GalacticraftForgeBootstrap.class' \
  'dev/galacticraft/api/registry/BuiltInAddonRegistries.class' \
  'dev/galacticraft/mod/content/GCBlocks.class' \
  'dev/galacticraft/mod/content/item/GCItems.class' \
  'dev/galacticraft/mod/content/entity/RocketEntity.class' \
  'dev/galacticraft/mod/content/block/entity/RocketWorkbenchBlockEntity.class' \
  'dev/galacticraft/mod/screen/GCMenuTypes.class'; do
  grep -Fxq "$entry" "$JAR_ENTRIES" || {
    echo "Missing required runtime entry: $entry" >&2
    exit 1
  }
done

grep -q '^assets/galacticraft/' "$JAR_ENTRIES"
grep -q '^data/galacticraft/' "$JAR_ENTRIES"

if grep -Eq '^(fabric\.mod\.json|galacticraft(-api)?\.mixins\.json|galacticraft\.accesswidener)$' "$JAR_ENTRIES"; then
  echo 'Fabric-only loader metadata leaked into the Forge runtime jar.' >&2
  exit 1
fi

printf '%s\n' "$RUNTIME_JAR" > "$OUT_DIR/runtime-jar-path.txt"
printf '%s\n' "$MACHINE_RUNTIME_JAR" > "$OUT_DIR/machinelib-jar-path.txt"
