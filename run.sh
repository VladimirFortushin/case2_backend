#!/bin/bash
set -e

docker compose down

docker compose up -d --build

docker ps --filter "name=hakaton-"