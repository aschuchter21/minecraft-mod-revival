#!/usr/bin/env bash
set -euo pipefail

RUNTIME_DIR="${1:-ForgeRuntimeValidation}"
LOG_FILE="$RUNTIME_DIR/server-smoke.log"
RUN_DIR="$RUNTIME_DIR/run-server"
MACHINE_RUNTIME_JAR="$RUNTIME_DIR/libs/machinelib-forge-runtime.jar"

mkdir -p "$RUN_DIR"
printf 'eula=true\n' > "$RUN_DIR/eula.txt"

# MachineLib is a mandatory Forge mod dependency of Galacticraft. The assembled
# workspace originally placed its Mojmap dev jar on plain runtimeOnly, which
# exposes classes but does not make FML discover modId "machinelib". For the
# smoke run, remove that plain runtime dependency and feed the production Forge
# jar through ModDevGradle's mod-aware remapping configuration instead. This is
# the same shape expected for ordinary external Forge mod dependencies.
[[ -s "$MACHINE_RUNTIME_JAR" ]]
cat >> "$RUNTIME_DIR/build.gradle" <<'GRADLE'

repositories {
    flatDir {
        dirs 'libs'
    }
}

// assemble-runtime.sh adds the Mojmap dev jar to runtimeOnly for classpath
// validation. Do not leave that duplicate beside the mod-remapped dependency.
configurations.runtimeOnly.dependencies.clear()

dependencies {
    modRuntimeOnly 'local:machinelib-forge-runtime:1.0'
}
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
