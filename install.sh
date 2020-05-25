#!/bin/bash

if [[ "$1" != "--skipsql" ]]; then
  docker-compose up -d mysql
  sleep 5
  cd app
  cd src/sql
  python3 run.py 1.0.0
  cd ../../..
  docker-compose down
fi

if [[ "$1" != "--skipgradle" ]]; then
  ./gradlew clean
  ./gradlew build
fi

cd app
docker build . --no-cache -t gatekeeper:latest
cd ..
