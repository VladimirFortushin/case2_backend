#!/bin/bash
set -e

# очистка video_stats, urls, users_urls и перезапуск инкремента id
#docker exec -i hakaton-db psql -U hakaton_user -d hakaton_db -c "TRUNCATE video_stats, urls, users_urls RESTART IDENTITY CASCADE;"
docker compose down -v