WebRTC-Observer Service
==
![Build](https://github.com/ObserveRTC/observer/actions/workflows/build.yml/badge.svg)
![Test](https://github.com/ObserveRTC/observer/actions/workflows/test.yml/badge.svg)

Observer is an Open Source monitoring service for WebRTC applications. It is part of 
a solution ObserveRTC offers for developers. 

WebRTC applications integrated with [observer.js](https://github.com/ObserveRTC/integrations)
receive their [PeerConnection Samples](https://observertc.org/docs/references/peer-connection-sample/)
at this service. The `observer` service is capable of groupping them into call, and
forwarding [WebRTC-Observer](https://github.com/ObserveRTC/webrtc-observer) to
defined sinks (e.g.: kafka).

## Quick Start

You can deploy the service by using [docker](https://github.com/ObserveRTC/docker-webrtc-observer)
or [helm](https://github.com/ObserveRTC/helm).
To integrate `observer-js`, please visit the integration [guidline](https://github.com/ObserveRTC/integrations)

## Installation

Please read [INSTALL](INSTALL.md) instructions.

## Test

The service uses `gradle` to build and test.
To simply run tests: `gradle test`


## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker.

## Getting involved

Click [here](https://github.com/ObserveRTC/observer/projects/5) and see if you are interested 
in one of the project we target to add. If you find there something interesting, then 
put it into ToDO, convert it to issue, and start working. 

If you have something in your mind to add, also add it in our [project](https://github.com/ObserveRTC/observer/projects/5) 
page.

Also, please read [CONTRIBUTING](CONTRIBUTING.md) guidline.

----

## Versions

The version number (more or less) follows [semantic versioning](https://semver.org/).
The major versions are named, and tagged.

Major versions:
* [hydrogen](hydrogen.md) The first viable service of the product


## Open source licensing info
=======

1. [LICENSE](LICENSE)




