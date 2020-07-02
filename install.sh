#!/bin/bash

#! /bin/sh -
set -x
# default value for service
SERVICE="webrtcstat"
DATASOURCE="mysql"
SUBSCRIBER=""
while test $# -gt 0; do
  case "$1" in
  --service)
    SERVICE="$2"
    SERVICE=$(echo "$SERVICE" | awk '{print tolower($0)}')
    if [[ "$SERVICE" =~ ^(webrtcstat)$ ]]; then
      echo "selected service is $SERVICE"
    else
      echo "unrecognized service $SERVICE"
      exit
    fi
    shift
    ;;
  --datasource)
    DATASOURCE="$2"
    DATASOURCE=$(echo "$DATASOURCE" | awk '{print tolower($0)}')
    if [[ "$DATASOURCE" =~ ^(mysql)$ ]]; then
      echo "selected datasource is $DATASOURCE"
    else
      echo "unrecognized datasource $DATASOURCE"
      exit
    fi
    shift
    ;;
  --subscriber)
    SUBSCRIBER="$2"
    SUBSCRIBER=$(echo "$SUBSCRIBER" | awk '{print tolower($0)}')
    if [[ "$SUBSCRIBER" =~ ^(elasticsearch)$ ]]; then
      echo "selected subscriber is $SUBSCRIBER"
    else
      echo "unrecognized subscriber $SUBSCRIBER"
      exit
    fi
    shift
    ;;
  *)
    echo "argument $1"
    ;;
  esac
  shift
done

CONFIGURATION_KEY=""
if [[ "$SERVICE" != "" && "$DATASOURCE" != "" && "$SUBSCRIBER" != "" ]]; then
  CONFIGURATION_KEY="${SERVICE}_${DATASOURCE}_${SUBSCRIBER}"
elif [[ "$SERVICE" != "" && "$DATASOURCE" != "" ]]; then
  CONFIGURATION_KEY="${SERVICE}_${DATASOURCE}"
else
  echo "mandatory keys: --datasource and --service"
fi

# now copy!
DEVOPS_BUILD="devopscripts/build"
cp $DEVOPS_BUILD/docker_compose/${CONFIGURATION_KEY}.yml docker-compose.yml
cp $DEVOPS_BUILD/used_profiles/${CONFIGURATION_KEY}.properties used-profiles.properties
cp $DEVOPS_BUILD/micronaut_application_yaml/${CONFIGURATION_KEY}.yaml $SERVICE/src/main/resources/application.yaml
cp $DEVOPS_BUILD/db_scripts/${SERVICE}/${DATASOURCE}.sql init.sql

docker-compose up -d $DATASOURCE
sleep 10

./gradlew clean
./gradlew build
cd $SERVICE
docker build . -t ${SERVICE}:latest
#./gradlew buildDockerImage
cd ..

docker-compose stop $DATASOURCE
docker-compose up -d kafka
sleep 10

docker-compose exec docker_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic ObserveRTCCIceStatsSample
docker-compose exec docker_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic observeRTCMediaStreamStatsSamples
docker-compose exec docker_kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic observerSSRCPeerConnectionSamples

docker-compose stop kafka
exit 0
