WebRTC-Observer Service
==
![Build](https://github.com/ObserveRTC/observer/actions/workflows/build.yml/badge.svg)
![Test](https://github.com/ObserveRTC/observer/actions/workflows/test.yml/badge.svg)


WebRTC applications integrated with [observer.js](https://github.com/ObserveRTC/integrations)
receive their [PeerConnection Samples](https://observertc.org/docs/references/peer-connection-sample/)
at this service. The `observer` service is capable of groupping them into call, and
forwarding [WebRTC-Observer](https://github.com/ObserveRTC/webrtc-observer) to
defined sinks (e.g.: kafka).

## Quick Start

You can deploy the service by using [docker](https://github.com/ObserveRTC/docker-webrtc-observer)
or [helm](https://github.com/ObserveRTC/helm).
To integrate `observer-js`, please visit the integration [guidline](https://github.com/ObserveRTC/integrations)


## Dependencies

Describe any dependencies that must be installed for this software to work.
This includes programming languages, databases or other storage mechanisms, build tools, frameworks, and so forth.
If specific versions of other software are required, or known not to work, call that out.

## Installation

Please read [INSTALL](INSTALL.md) instructions.

## How to test the software

The service uses `gradle` to build and test.
To simply run tests: `gradle test`

## Known issues

TBD

## Getting help

If you have questions, concerns, bug reports, etc, please file an issue in this repository's Issue Tracker.

## Getting involved

We currently focusing on the following areas of development this service:
* Better documentation
* Improve test coverage
* Improve call monitoring capabilities
* Improve automatic hazelcast migration between different version of observer
* Add repository cleaner for stalled calls
* Improve configuration structure

If you would like to contribute, first of all many thanks,
second of all, please read [CONTRIBUTING](CONTRIBUTING.md) guidline.

----

## Configurations

Under the hood the `observer` is built by using:
* [micronaut](https://micronaut.io) framework and using its plugin for authentication, websocket provision, security, etc.,  thus you can configure the framework by providing the proper yaml (or json) configuration in the `application.yaml`.
* [Hazelcast](https://hazelcast.org) in memory grid, thus the proper hazelcast configuration is necessary to configure the memory grid in clusters.

Observer specific configuration can be found in the `application.yaml` under the `observer` entry.

```yaml
observer:
  
  # Setup security configuration specific to the observer service
  security:
    # Drop Peer Connection Samples, for which the service uuid does not match any service name
    dropUnknownServices: False
  
  # Sets up the mapping between service UUID and service Name.
  servicemappings:
    - name: "example-service-name"
      uuids:
        - "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"
  
  sentinelFilters:
    - name: "MyPeerToPeerFilter"
      serviceName: "example-service-name"
      browserIds:
        gt: 0 # greater then
        lt: 3 # less then
  
  # A configured sentinel exposing metrics about the filtered calls
  sentinels:
    - name: "MySentinel"
      anyMatchFilters:
        - "MyPeerToPeerFilter"
      allMatchFilters:
        - "MyOtherFilter"
  
  # The time Period sentinels are checking calls in minutes
  sentinelsCheckingPeriodInMin: 1
  
  # Defines the configuration for a connector the reports are sent to
  connectors:
    - name: "ReportSinkLogger"
      buffer:
        maxItems: 100
        maxWaitingTimeInS: 10
      sink:
        type: LoggerSink
        config:
          printReports: False

  # Sets up the Evaluators every incoming sample is subjected
  evaluators:
    
    # name of the calls, which cannot be paired (nor SSRC, neither call name was provided to match)
    impairablePCsCallName: "impairable-peer-connections-default-call-name"
    
    # The incoming sample buffer maximum waiting time before emission
    observedPCSBufferMaxTimeInS: 10
    
    # The incoming sample buffer maximum amount of items it can hold
    observedPCSBufferMaxItemNums: 10000
    
    # The maximum idle time for a peer connection before it is declared to be detached.
    peerConnectionMaxIdleTimeInS: 60
  
  # Sets up which type of webrtc reports the service can forward
  outboundReports:
    reportOutboundRTPs: True
    reportInboundRTPs: True
    reportRemoteInboundRTPs: True
    reportTracks: True
    reportMediaSources: True
    reportCandidatePairs: True
    reportLocalCandidates: True
    reportRemoteCandidates: True
    reportUserMediaErrors: True
    
  # Sets up the hazelcast configuration file
  hazelcast:
    configFile: ${HAZELCAST_CONFIG_FILE:`classpath:hazelcast.yaml`}
```

----

## Open source licensing info

1. [LICENSE](LICENSE)




