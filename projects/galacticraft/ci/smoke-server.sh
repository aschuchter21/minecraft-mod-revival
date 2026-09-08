#!/usr/bin/env bash
set -euo pipefail

RUNTIME_DIR="${1:-ForgeRuntimeValidation}"
LOG_FILE="$RUNTIME_DIR/server-smoke.log"
RUN_DIR="$RUNTIME_DIR/run-server"

mkdir -p "$RUN_DIR"
printf 'eula=true\n' > "$RUN_DIR/eula.txt"

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
