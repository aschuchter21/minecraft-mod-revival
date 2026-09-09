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
RUNTIME_SRC="$OUT_DIR/src/main/java"

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
cp -R "$OVERLAY_SRC/." "$RUNTIME_SRC/"

# GCBlocks is still mostly loader-neutral upstream code, but its Fabric content
# registry calls are loader boundaries. Generate the Forge source from the exact
# recovered 1.20.1 source and replace those calls with native Forge/vanilla
# behavior. Moon dirt flattening is handled by GalacticraftForgeBlockHooks.
GC_BLOCKS_UPSTREAM="$UPSTREAM_GC/src/main/java/dev/galacticraft/mod/content/GCBlocks.java"
GC_BLOCKS_FORGE="$RUNTIME_SRC/dev/galacticraft/mod/content/GCBlocks.java"
mkdir -p "$(dirname "$GC_BLOCKS_FORGE")"
python3 - "$GC_BLOCKS_UPSTREAM" "$GC_BLOCKS_FORGE" <<'PY'
from pathlib import Path
import sys

source_path = Path(sys.argv[1])
out_path = Path(sys.argv[2])
source = source_path.read_text()

replacements = [
    ("import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;\n", ""),
    ("import net.fabricmc.fabric.api.registry.FlattenableBlockRegistry;\n", ""),
    (
        "        FlammableBlockRegistry.getDefaultInstance().add(FUEL, 80, 130);",
        "        ((FireBlock) Blocks.FIRE).setFlammable(FUEL, 80, 130);",
    ),
    (
        "        FlammableBlockRegistry.getDefaultInstance().add(CRUDE_OIL, 60, 100);",
        "        ((FireBlock) Blocks.FIRE).setFlammable(CRUDE_OIL, 60, 100);",
    ),
    (
        "        FlammableBlockRegistry.getDefaultInstance().add(CAVERNOUS_VINES, 15, 60);",
        "        ((FireBlock) Blocks.FIRE).setFlammable(CAVERNOUS_VINES, 15, 60);",
    ),
    (
        "        FlammableBlockRegistry.getDefaultInstance().add(CAVERNOUS_VINES_PLANT, 15, 60);",
        "        ((FireBlock) Blocks.FIRE).setFlammable(CAVERNOUS_VINES_PLANT, 15, 60);",
    ),
    (
        "        FlattenableBlockRegistry.register(MOON_DIRT, MOON_DIRT_PATH.defaultBlockState());\n",
        "",
    ),
]

for old, new in replacements:
    if old not in source:
        raise SystemExit(f"Expected GCBlocks Forge-port source pattern not found: {old!r}")
    source = source.replace(old, new, 1)

if "net.fabricmc.fabric.api.registry" in source:
    raise SystemExit("Fabric registry API still referenced by generated Forge GCBlocks source")

out_path.write_text(source)
PY

# GCItems is likewise loader-neutral except for FabricItemSettings on the three
# air-lock BlockItems. FabricItemSettings only supplied the ordinary item
# properties here, so use Minecraft's Item.Properties directly on Forge.
GC_ITEMS_UPSTREAM="$UPSTREAM_GC/src/main/java/dev/galacticraft/mod/content/item/GCItems.java"
GC_ITEMS_FORGE="$RUNTIME_SRC/dev/galacticraft/mod/content/item/GCItems.java"
mkdir -p "$(dirname "$GC_ITEMS_FORGE")"
python3 - "$GC_ITEMS_UPSTREAM" "$GC_ITEMS_FORGE" <<'PY'
from pathlib import Path
import sys

source_path = Path(sys.argv[1])
out_path = Path(sys.argv[2])
source = source_path.read_text()

fabric_import = "import net.fabricmc.fabric.api.item.v1.FabricItemSettings;\n"
if fabric_import not in source:
    raise SystemExit("Expected FabricItemSettings import not found in recovered GCItems source")
source = source.replace(fabric_import, "", 1)

settings_call = "new FabricItemSettings()"
settings_count = source.count(settings_call)
if settings_count != 3:
    raise SystemExit(f"Expected exactly 3 FabricItemSettings usages in recovered GCItems, found {settings_count}")
source = source.replace(settings_call, "new Item.Properties()")

if "net.fabricmc.fabric.api.item" in source or "FabricItemSettings" in source:
    raise SystemExit("Fabric item API still referenced by generated Forge GCItems source")

out_path.write_text(source)
PY

# GCBlockEntityTypes used FabricBlockEntityTypeBuilder solely as a thin wrapper
# around Minecraft's own BlockEntityType.Builder. Rebuild the exact recovered
# declarations with the vanilla builder so the Forge runtime has no Fabric ABI
# dependency at this registry boundary.
GC_BE_TYPES_UPSTREAM="$UPSTREAM_GC/src/main/java/dev/galacticraft/mod/content/GCBlockEntityTypes.java"
GC_BE_TYPES_FORGE="$RUNTIME_SRC/dev/galacticraft/mod/content/GCBlockEntityTypes.java"
mkdir -p "$(dirname "$GC_BE_TYPES_FORGE")"
python3 - "$GC_BE_TYPES_UPSTREAM" "$GC_BE_TYPES_FORGE" <<'PY'
from pathlib import Path
import sys

source_path = Path(sys.argv[1])
out_path = Path(sys.argv[2])
source = source_path.read_text()

fabric_import = "import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;\n"
if fabric_import not in source:
    raise SystemExit("Expected FabricBlockEntityTypeBuilder import not found in recovered GCBlockEntityTypes source")
source = source.replace(fabric_import, "", 1)

builder_call = "FabricBlockEntityTypeBuilder.create("
builder_count = source.count(builder_call)
if builder_count < 1:
    raise SystemExit("No FabricBlockEntityTypeBuilder declarations found in recovered GCBlockEntityTypes")

build_count = source.count(".build();")
if build_count != builder_count:
    raise SystemExit(
        f"Unexpected GCBlockEntityTypes builder shape: {builder_count} create calls but {build_count} build calls"
    )

source = source.replace(builder_call, "BlockEntityType.Builder.of(")
source = source.replace(".build();", ".build(null);")

if "FabricBlockEntityTypeBuilder" in source or "net.fabricmc.fabric.api.object.builder" in source:
    raise SystemExit("Fabric block-entity builder API still referenced by generated Forge GCBlockEntityTypes source")

out_path.write_text(source)
PY

# All known server-side uses of the temporary Fabric migration ABI have now been
# replaced by native Forge/vanilla code. Do not compile those compatibility shim
# packages into the runtime at all.
rm -rf "$RUNTIME_SRC/net/fabricmc"

# The support jar contains the recovered upstream implementation. Extract it so
# loader-neutral classes remain available to the Forge runtime.
(
  cd "$OUT_DIR/upstream-classes"
  jar xf ../libs/gc-1.20.1-support.jar
)

# IMPORTANT: never leave two definitions of a source-ported class on the runtime
# classpath. Remove every recovered class that has a Forge runtime source,
# including compiler-generated nested/anonymous classes. The freshly compiled
# Forge source is then the sole runtime definition.
REMOVED_OVERLAY_CLASSES=0
while IFS= read -r source; do
  rel="${source#"$RUNTIME_SRC/"}"
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
done < <(find "$RUNTIME_SRC" -type f -name '*.java' -print | sort)

echo "Removed $REMOVED_OVERLAY_CLASSES recovered class files superseded by Forge runtime sources."

# Fail assembly immediately if any recovered duplicate survives the pruning pass.
while IFS= read -r source; do
  rel="${source#"$RUNTIME_SRC/"}"
  stem="${rel%.java}"
  class_file="$OUT_DIR/upstream-classes/$stem.class"
  class_dir=$(dirname "$class_file")
  class_base=$(basename "$stem")

  if [[ -f "$class_file" ]]; then
    echo "Recovered duplicate survived for Forge runtime source: $rel" >&2
    exit 1
  fi
  if [[ -d "$class_dir" ]] && find "$class_dir" -maxdepth 1 -type f -name "${class_base}\$*.class" -print -quit | grep -q .; then
    echo "Recovered nested duplicate survived for Forge runtime source: $rel" >&2
    exit 1
  fi
done < <(find "$RUNTIME_SRC" -type f -name '*.java' -print | sort)

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

    // ModDev userdev runs in Mojmap. The production MachineLib jar is already
    // reobfuscated to SRG and must never be placed on the development runServer
    // classpath; the real production smoke installs that jar separately.
    runtimeOnly files('libs/machinelib-forge-dev.jar')
}

tasks.named('jar') {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
GRADLE

echo '--- Runtime overlay sources ---'
find "$RUNTIME_SRC" -type f -name '*.java' -printf '%P\n' | sort

echo '--- Runtime libraries ---'
find "$OUT_DIR/libs" -maxdepth 1 -type f -printf '%f\n' | sort

gradle -p "$OUT_DIR" clean build --stacktrace

RUNTIME_JAR=$(find "$OUT_DIR/build/libs" -maxdepth 1 -type f \
  -name 'Galacticraft-Forge-1.20.1-*.jar' ! -name '*-sources.jar' | head -n 1)
[[ -n "$RUNTIME_JAR" && -s "$RUNTIME_JAR" ]]

# Recovered classes should now be physically staged beside compiled overlays,
# rather than represented as an auxiliary SourceSet output directory.
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/GCBlocks.class" ]]
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/item/GCItems.class" ]]
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/GCBlockEntityTypes.class" ]]
[[ -f "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/block/entity/RocketWorkbenchBlockEntity.class" ]]

if strings "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/GCBlocks.class" | grep -q 'net/fabricmc/fabric/api/registry'; then
  echo 'Fabric registry API leaked into Forge GCBlocks.class.' >&2
  exit 1
fi
if strings "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/item/GCItems.class" | grep -Eq 'net/fabricmc/fabric/api/item|FabricItemSettings'; then
  echo 'Fabric item API leaked into Forge GCItems.class.' >&2
  exit 1
fi
if strings "$OUT_DIR/build/classes/java/main/dev/galacticraft/mod/content/GCBlockEntityTypes.class" | grep -Eq 'net/fabricmc/fabric/api/object/builder|FabricBlockEntityTypeBuilder'; then
  echo 'Fabric block-entity builder API leaked into Forge GCBlockEntityTypes.class.' >&2
  exit 1
fi

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
  'dev/galacticraft/forge/GalacticraftForgeBlockHooks.class' \
  'dev/galacticraft/api/registry/BuiltInAddonRegistries.class' \
  'dev/galacticraft/mod/content/GCBlocks.class' \
  'dev/galacticraft/mod/content/item/GCItems.class' \
  'dev/galacticraft/mod/content/GCBlockEntityTypes.class' \
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

if grep -q '^net/fabricmc/' "$JAR_ENTRIES"; then
  echo 'Fabric compatibility shim classes leaked into the Forge runtime jar:' >&2
  grep '^net/fabricmc/' "$JAR_ENTRIES" >&2
  exit 1
fi

printf '%s\n' "$RUNTIME_JAR" > "$OUT_DIR/runtime-jar-path.txt"
printf '%s\n' "$MACHINE_RUNTIME_JAR" > "$OUT_DIR/machinelib-jar-path.txt"
