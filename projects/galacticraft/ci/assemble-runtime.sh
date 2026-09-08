#!/usr/bin/env bash
set -euo pipefail

# Assemble the first runnable Forge-facing Galacticraft artifact from the exact
# recovered 1.20.1 classes plus the source-ported Forge overlay. This script is
# intentionally CI-friendly: it never mutates the recovered upstream checkout.

REVIVAL_ROOT="${1:-Revival}"
UPSTREAM_GC="${2:-Galacticraft}"
SUPPORT_JAR="${3:-gc-1.20.1-support.jar}"
OUT_DIR="${4:-ForgeRuntimeValidation}"

MACHINE_BUILD="$REVIVAL_ROOT/projects/machinelib/forge-backport/build/libs"
MACHINE_DEV_JAR="$MACHINE_BUILD/MachineLib-Forge-1.20.1-dev.jar"
MACHINE_RUNTIME_JAR=$(find "$MACHINE_BUILD" -maxdepth 1 -type f \
  -name 'MachineLib-Forge-1.20.1-*.jar' \
  ! -name '*-sources.jar' ! -name '*-dev.jar' | head -n 1)

[[ -s "$SUPPORT_JAR" ]]
[[ -s "$MACHINE_DEV_JAR" ]]
[[ -n "$MACHINE_RUNTIME_JAR" && -s "$MACHINE_RUNTIME_JAR" ]]

rm -rf "$OUT_DIR"
cp -R "$REVIVAL_ROOT/projects/galacticraft/forge-skeleton" "$OUT_DIR"
mkdir -p "$OUT_DIR/libs" "$OUT_DIR/upstream-classes"

cp "$SUPPORT_JAR" "$OUT_DIR/libs/gc-1.20.1-support.jar"
cp "$MACHINE_DEV_JAR" "$OUT_DIR/libs/machinelib-forge-dev.jar"
cp "$MACHINE_RUNTIME_JAR" "$OUT_DIR/libs/machinelib-forge-runtime.jar"

# Overlay source is compiled normally and therefore wins over same-named classes
# recovered from upstream.
cp -R "$REVIVAL_ROOT/projects/galacticraft/forge-overlays/src/main/java/." \
  "$OUT_DIR/src/main/java/"

# The support jar contains only upstream compiled classes. Add them as an extra
# SourceSet output directory so both runServer and the reobfuscated production jar
# see them, without asking ModDevGradle to remap an already-named support jar.
(
  cd "$OUT_DIR/upstream-classes"
  jar xf ../libs/gc-1.20.1-support.jar
)

# Bring over only loader-neutral game resources for the first runtime test.
# Fabric metadata, access widener and Fabric mixin configs are deliberately not
# copied; those subsystems are being ported independently.
cp -R "$UPSTREAM_GC/src/main/resources/assets" "$OUT_DIR/src/main/resources/"
cp -R "$UPSTREAM_GC/src/main/resources/data" "$OUT_DIR/src/main/resources/"
cp "$UPSTREAM_GC/src/main/resources/pack.mcmeta" "$OUT_DIR/src/main/resources/pack.mcmeta"

cat >> "$OUT_DIR/build.gradle" <<'GRADLE'

sourceSets {
    main {
        output.dir(file('upstream-classes'))
    }
}

dependencies {
    compileOnly files('libs/gc-1.20.1-support.jar', 'libs/machinelib-forge-dev.jar')
    runtimeOnly files('libs/machinelib-forge-runtime.jar')
}

tasks.named('jar') {
    // Compiled Forge overlays are part of the normal SourceSet output first;
    // duplicate upstream classes from upstream-classes are ignored afterward.
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

echo "Experimental Galacticraft runtime: $RUNTIME_JAR"
echo "MachineLib runtime: $MACHINE_RUNTIME_JAR"

# Assert that this is a real assembled runtime rather than the old skeleton jar.
for entry in \
  'META-INF/mods.toml' \
  'META-INF/accesstransformer.cfg' \
  'dev/galacticraft/forge/GalacticraftForgeBootstrap.class' \
  'dev/galacticraft/mod/content/GCBlocks.class' \
  'dev/galacticraft/mod/content/item/GCItems.class' \
  'dev/galacticraft/mod/content/entity/RocketEntity.class'; do
  jar tf "$RUNTIME_JAR" | grep -qx "$entry" || {
    echo "Missing required runtime entry: $entry" >&2
    exit 1
  }
done

jar tf "$RUNTIME_JAR" | grep -q '^assets/galacticraft/'
jar tf "$RUNTIME_JAR" | grep -q '^data/galacticraft/'

if jar tf "$RUNTIME_JAR" | grep -Eq '^(fabric\.mod\.json|galacticraft(-api)?\.mixins\.json|galacticraft\.accesswidener)$'; then
  echo 'Fabric-only loader metadata leaked into the Forge runtime jar.' >&2
  exit 1
fi

printf '%s\n' "$RUNTIME_JAR" > "$OUT_DIR/runtime-jar-path.txt"
printf '%s\n' "$MACHINE_RUNTIME_JAR" > "$OUT_DIR/machinelib-jar-path.txt"
