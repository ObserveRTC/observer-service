#!/bin/bash

docker-compose down
docker-compose stop mysql
docker-compose up -d mysql &&
  sleep 10 &&
  ./gradlew clean build &&
  docker-compose stop mysql

docker build -f ObserverBuilder.dockerfile . -t webrtc-observer:latest
docker build -f BigQueryReporterBuilder.dockerfile . -t webrtc-bigquery-reporter:latest
