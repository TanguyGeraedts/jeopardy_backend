#!/usr/bin/env bash
# Nightly database dump, run by root's cron (see README.md). Keeps the last 7 days in /opt/jeopardy/backups.
# Restore: gunzip -c <file> | docker compose -f /opt/jeopardy/docker-compose.yml exec -T postgres psql -U jeopardy jeopardy
set -euo pipefail

APP_DIR=/opt/jeopardy
BACKUP_DIR="$APP_DIR/backups"
KEEP_DAYS=7

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"

file="$BACKUP_DIR/jeopardy-$(date +%Y%m%d-%H%M%S).sql.gz"
# Written to a temp name first, so a failed dump never looks like a good backup.
docker compose -f "$APP_DIR/docker-compose.yml" exec -T postgres pg_dump -U jeopardy --clean --if-exists jeopardy | gzip > "$file.tmp"
mv "$file.tmp" "$file"

find "$BACKUP_DIR" -name 'jeopardy-*.sql.gz' -mtime +"$KEEP_DAYS" -delete
