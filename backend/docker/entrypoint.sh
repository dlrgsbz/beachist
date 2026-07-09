#!/usr/bin/env bash
set -euo pipefail

# Ensure dependencies exist (they may be missing when the source is mounted as a volume).
if [ ! -f vendor/autoload.php ]; then
  echo "[entrypoint] Installing composer dependencies..."
  composer install --no-scripts --no-plugins --no-interaction --prefer-dist --no-progress
fi

# Wait for the database to accept connections.
if [ -n "${DATABASE_HOST:-}" ]; then
  echo "[entrypoint] Waiting for database at ${DATABASE_HOST}:${DATABASE_PORT:-3306}..."
  until mysqladmin ping -h "${DATABASE_HOST}" -P "${DATABASE_PORT:-3306}" --silent >/dev/null 2>&1; do
    sleep 2
  done
  echo "[entrypoint] Database is up."
fi

# Apply database migrations.
echo "[entrypoint] Running migrations..."
php bin/console doctrine:migrations:migrate --no-interaction --allow-no-migration

# Optionally seed example data (users + stations + items + assignments).
if [ "${SEED_EXAMPLE_DATA:-0}" = "1" ]; then
  echo "[entrypoint] Seeding example data..."
  php seed_example_data.php
fi

echo "[entrypoint] Starting: $*"
exec "$@"
 