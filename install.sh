#!/bin/bash

VERSION=0.3

docker login

docker build -f ObserverBuilder.dockerfile . -t observertc/webrtc-observer:$VERSION
docker push observertc/webrtc-observer:$VERSION

#docker build -f ReporterBuilder.dockerfile . -t observertc/webrtc-reporter:$VERSION
#docker push observertc/webrtc-reporter:$VERSION
