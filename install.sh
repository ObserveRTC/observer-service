#!/bin/bash

VERSION=0.5.1

docker login

docker build -f ObserverBuilder.dockerfile . -t observertc/webrtc-observer:$VERSION
docker push observertc/webrtc-observer:$VERSION
