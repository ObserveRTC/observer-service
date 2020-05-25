#!/bin/bash

docker-compose up -d mysql
if [[ "$1" != "--skipsql" ]]; then
  sleep 30
  cd app
  cd src/sql
  python3 run.py 1.0.0
  cd ../../..
else
  sleep 10
fi

if [[ "$1" != "--skipgradle" ]]; then
  ./gradlew clean
  ./gradlew build
fi

cd app
docker build . --no-cache -t gatekeeper:latest
cd ..

docker-compose stop mysql
