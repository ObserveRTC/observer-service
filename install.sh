#!/bin/bash

VERSION=0.6-alpha

docker login

# docker build -f ObserverBuilder.dockerfile . -t observertc/webrtc-observer:$VERSION
gradle dockerBuild
docker push observertc/webrtc-observer:$VERSION
