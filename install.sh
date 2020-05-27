#!/bin/bash
if hash python3 2>/dev/null; then
  echo "Python3 has been found"
else
  echo "Python3 package does not exist"
  exit
fi

if hash pip3 2>/dev/null; then
  echo "pip3 has been found"
else
  echo "pip3 package does not exist"
  exit
fi

# If someone wants to install only in locally, then virtualenv should be used.
# pip3 install -r devopscripts/python_requirements/requirements.txt -t devopscripts/libs
pip3 install -r devopscripts/requirements.txt

cd devopscripts
python3 install.py $@

cp build/application.yaml ../webrtcstat/src/main/resources/application.yaml
cp build/docker-compose.yaml ../docker-compose.yaml
cp build/used-profiles.properties ../used-profiles.properties
source build/postscript.sh
cd ..

#
#docker-compose up -d mysql
#if [[ "$1" != "--skipsql" ]]; then
#  sleep 30
#  cd app
#  cd src/sql
#  python3 run.py 1.0.0
#  cd ../../..
#else
#  sleep 10
#fi
#
#if [[ "$1" != "--skipgradle" ]]; then
#  ./gradlew clean
#  ./gradlew build
#fi
#
#cd app
#docker build . --no-cache -t gatekeeper:latest
#cd ..
#
#docker-compose stop mysql
