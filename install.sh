#!/bin/bash

docker-compose stop mysql
docker-compose up -d mysql &&
  sleep 10 &&
  ./gradlew clean build &&
  docker-compose stop mysql
