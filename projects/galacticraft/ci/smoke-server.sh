#!/usr/bin/env bash
set -euo pipefail

RUNTIME_DIR="${1:-ForgeRuntimeValidation}"
LOG_FILE="$RUNTIME_DIR/server-smoke.log"
RUN_DIR="$RUNTIME_DIR/run-server"
MACHINE_DEV_JAR="$RUNTIME_DIR/libs/machinelib-forge-dev.jar"

mkdir -p "$RUN_DIR/mods"
printf 'eula=true\n' > "$RUN_DIR/eula.txt"

# The ModDevGradle server runs in Mojmap. MachineLib therefore must be the
# unreobfuscated development jar, not the production SRG/reobfuscated jar.
# Put that dev jar in the actual mods directory so FML discovers its mods.toml
# as modId "machinelib" instead of treating it as a plain classpath library.
[[ -s "$MACHINE_DEV_JAR" ]]
jar tf "$MACHINE_DEV_JAR" | grep -qx 'META-INF/mods.toml'
rm -f "$RUN_DIR/mods"/machinelib*.jar
cp "$MACHINE_DEV_JAR" "$RUN_DIR/mods/machinelib-forge-dev.jar"

# assemble-runtime.sh also places the same dev classes on runtimeOnly so Forge
# overlays can link during build validation. Remove that plain runtime classpath
# dependency for runServer; the mods-directory copy is now the sole runtime
# MachineLib definition and is discoverable by FML.
cat >> "$RUNTIME_DIR/build.gradle" <<'GRADLE'

configurations.runtimeOnly.dependencies.clear()
GRADLE

set +e
timeout 150s gradle -p "$RUNTIME_DIR" runServer --stacktrace > "$LOG_FILE" 2>&1
STATUS=$?
set -e

# A dedicated server intentionally remains alive after a successful start, so a
# timeout is expected. Minecraft's standard "Done (...)!" log line is the gate.
if grep -Fq 'Done (' "$LOG_FILE"; then
  echo 'Forge server smoke boot reached the running-server state.'
  tail -n 120 "$LOG_FILE"
  exit 0
fi

echo "Forge server smoke boot did not reach the running-server state (exit $STATUS)." >&2
tail -n 250 "$LOG_FILE" >&2
exit 1
