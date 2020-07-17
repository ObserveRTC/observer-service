#!/bin/bash

docker-compose down
docker-compose stop mysql
docker-compose up -d mysql &&
  sleep 10 &&
  ./gradlew clean build &&
  docker-compose stop mysql

cd service
docker build . -t webrtc_observer:latest
#./gradlew buildDockerImage
cd ..
