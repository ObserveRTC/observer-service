#!/bin/bash

docker-compose down
docker-compose stop mysql
docker-compose up -d mysql &&
  sleep 10 &&
  ./gradlew clean build &&
  docker-compose stop mysql

cd observer
docker build . -t webrtc-observer:latest
#./gradlew buildDockerImage
cd ..

cd bigquery-reporter
docker build . -t webrtc-bigquery-reporter:latest
#./gradlew buildDockerImage
cd ..
