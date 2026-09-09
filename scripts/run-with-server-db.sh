#!/usr/bin/env bash
# Runs backend_java locally against the PRODUCTION database on the server, instead of a
# local MySQL instance — for a developer who wants to work against real data without
# importing a dump. Read/write: this is the live database everyone else's app instance
# also uses, so be careful with anything you run against it.
#
# Requires the DB-only SSH tunnel to be up first — from the aqqum root:
#   ./connect-server-db.sh
# (leave that running in its own terminal)
set -euo pipefail
cd "$(dirname "$0")/.."

ENV_FILE="server-db.env"
if [ ! -f "$ENV_FILE" ]; then
  echo "Missing $ENV_FILE — copy server-db.env.example to server-db.env and fill in the real DB password first." >&2
  exit 1
fi
set -a
source "$ENV_FILE"
set +a

: "${DB_URL:?DB_URL not set in $ENV_FILE}"
: "${DB_USERNAME:?DB_USERNAME not set in $ENV_FILE}"
: "${DB_PASSWORD:?DB_PASSWORD not set in $ENV_FILE}"

# Forced AFTER sourcing server-db.env, and as a real env var (not templated in
# application.properties) so it wins regardless of what ddl-auto is set to in anyone's
# local working copy — Spring Boot's env vars outrank application.properties. This is the
# shared production database everyone else's app instance also uses; a personal
# ddl-auto=update left over from local-only development has already once silently
# re-added columns another migration had deliberately dropped here. validate (not none)
# is deliberate: it still fails loudly at startup if your local entities have drifted
# from the live schema, which is useful signal — it just never *writes* anything.
export SPRING_JPA_HIBERNATE_DDL_AUTO=validate

echo "Connecting to: $DB_URL (make sure the tunnel from connect-server-db.sh is running)"
echo "ddl-auto forced to 'validate' for this run — schema changes against this DB go through a migration file, never auto-generated DDL."
export DB_URL DB_USERNAME DB_PASSWORD
./mvnw spring-boot:run
