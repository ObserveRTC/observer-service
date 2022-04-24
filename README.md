WebRTC-Observer
==
![Build](https://github.com/ObserveRTC/observer/actions/workflows/build.yml/badge.svg)
![Test](https://github.com/ObserveRTC/observer/actions/workflows/test.yml/badge.svg)

Observer is a stand-alone application. You can run it in a docker container with a command `docker run observertc/observer`. 
The service by default is listening on the port `7080`.

To see ObserveRTC integrations and examples, check out the [full stack examples repository](https://github.com/ObserveRTC/full-stack-examples).

### Table of Contents:
 * [Overview](#overview)
 * [Configurations](#configurations)
   * [Micronaut configs](#micronaut-configs)
   * [Management endpoints configs](#management-endpoints-configs)
   * [Observer configs](#observer-configs)
 * [Performance](#performance)
 * [Compatibility](#compatibility)
 * [Getting Involved](#getting-involved)
 * [Changelog](#changelog)
 * [License](#license)

## Overview

![Overview](docs/images/superficial-overview.png)

Observer is a server-side service for monitoring WebRTC applications. The service receives [Samples]()
from [Monitors](). Based on the received samples, the observer:

 * Identify calls
 * Create events (call started, call ended, client joined, client left, etc.)
 * Match clients' inbound-rtp sessions with remote clients' outbound-rtp sessions
 * Match SFUs RTP sessions with browser-side clients' in-, and outbound-rtp sessions.


### Receiving Samples

The observer accepts Samples on the following endpoints:

* **REST**: `http(s)://${OBSERVER_ADDRESS}:{PORT}/rest/samples/{serviceId}/{mediaUnitId}`
* **Websocket**: `ws(s)://${OBSERVER_ADDRESS}:{PORT}/samples/{serviceId}/{mediaUnitId}`

### Sending Reports

The observer assemble [Reports]() based on the obtained events, matched sessions and received samples.
The reports are forwarded to [Sinks](#sinks).  Currently, the following type of sinks are supported:
* [KafkaSink](#kafkasink): Apache Kafka Integration
* [MongoSink](#mongosink): Mongo Database integration

## Configurations

![Overview](docs/images/configuration-overview.png)

At startup time the application fetches the configuration and sets up the service. 
The default configuration the observer starts with 
can be found [here](https://github.com/ObserveRTC/observer/blob/master/src/main/resources/application.yaml).

The configuration file is divided to the following main parts:
 * **micronaut**: defines the server base properties (listening port, REST authentication methods, etc...)
 * **endpoints**: defines the management endpoints of the service.
 * **observer**:  defines the observer functionalities

### Micronaut configs

The application is written by using [micronaut](https://micronaut.io) framework. Micronaut defines the base 
properties of the application including which port it listens on for REST or websocket requests.
The complete guide to configure a micronaut application can be found [here](https://docs.micronaut.io/3.4.1/guide/configurationreference.html). 

Here is an example for the micronaut configuration part:

```yaml
micronaut:
  server:
    # defines the port the service is listening on 
    port: 7080
  metrics:
    # enable metric collection of the service (cpu usage, memory usage, etc.)
    enabled: True
    export:
      # Sets an exporting methods of the collected metrics
      prometheus:
        enabled: true
        descriptions: true
        # Sets the frequency to collect the metrics
        step: PT1M
  # Sets authentication methods to REST API endpoints
  # for complete guideline to setup the security for REST API requests check the
  # documentations https://micronaut-projects.github.io/micronaut-security/3.4.1/guide/configurationreference.html
  security:
    enabled: false
```

#### Management endpoints configs

The application have management endpoints. Management endpoints can be reached on a different port number than 
other micronaut endpoints. This allows for example to integrate the service into a container orchestration systems like 
kubernetes. 

Here is an example for the endpoints configuration part:

```yaml
endpoints:
  all:
    # defines the port number for the management endpoints
    port: 7081
  prometheus:
    # indicate if /metrics endpoint is provided or not
    enabled: true
    # indicate if the provided prometheus endpoint requires authentication or not
    # the authentication method is defined in the micronaut.security part
    sensitive: false
  health:
    # indicate if the service provide /heath endpoint.
    enabled: true
```

### Observer configs

The observer configuration can be divided into the following components:
 * **security**: define properties of security related processes (hash algorithm, hash salt, allowed serviceIds, etc.). 
 * **sources**: define properties of endpoints collecting Samples (for example enable or disable collecting for REST or Websocket) 
 * **sinks**: define connections observer generated Reports are forwarded to (mongoDB, kafka, etc.).
 * **repository** define properties of the repository the observer stores data in (media track expiration time, peer connection expiration time).
 * **buffers** defines the properties of the internal buffers (samples buffering time, reports buffering time, etc.)
 * **hazelcast**:  defines the settings of the [hazelcast](https://hazelcast.com) the service uses.

Here is an example for the observer configuration part:

```yaml
observer:
  
  sources:
    # settings related to REST API accepting Samples
    rest:
      # indicate if Samples are accepted through REST or not
      enabled: true
    # settings related to Websockets accepting Samples
    websocket:
      # indicate if Samples are accepted through websockets or not
      enabled: true

  repository:
    # the maximum idle time (no new sample received to update that track) 
    # for a media track object after which it is removed from the repository
    mediaTracksMaxIdleTimeInS: 300
    # the maximum idle time for a client object after which it is removed from the repository
    clientMaxIdleTimeInS: 300
    # the maximum idle time for an SFU object after which it is removed from the repository
    sfuMaxIdleTimeInS: 60
    # the maximum idle time for a Sfu Transport object after which it is removed from the repository
    sfuTransportMaxIdleTimeInS: 600
    # the maximum idle time for an SFU Rtp pad after which it is removed from the repository
    sfuRtpPadMaxIdleTimeInS: 600
    # the maximum idle time for an peer connection object after  which it is removed from the repository
    peerConnectionsMaxIdleTime: 300

  buffers:
    # settings of the buffer collect samples from the sources
    samplesCollector:
      # the maximum number of items of the buffer
      maxItems: 10000
      # the max time the buffer can hold an item in milliseconds
      maxTimeInMs: 10000
    # settings of the buffer holds item between evaluators
    debouncers:
      # the maximum number of items of the buffer
      maxItems: 10000
      # the max time the buffer can hold an item in milliseconds
      maxTimeInMs: 1000
    # settings of the buffer collect reports from evaluators
    reportsCollector:
      # the maximum number of items of the buffer
      maxItems: 10000
      # the max time the buffer can hold an item in milliseconds
      maxTimeInMs: 10000

  # sets up the security methods and values for the observer analysis
  security:
    # flag indicating if fetched configurations should be written to the console or not
    printConfigs: True
    # obfuscation methods related settings
    obfuscations:
      # the used hash algorithm for anonymization 
      hashAlgorithm: SHA-256
      # the added extra salt for the hash in anonymization process
      hashSalt: "MySalt"

  # settings for analysing Samples
  evaluators:
     # settings for component responsible for updating call entity objects 
    callUpdater:
       # defines the strategy to assign callId to clients.
       # possible values:
       #  - MASTER: the Observer responsible to assign new callId to clients, 
       #            and only the observer can end a call
       # - SLAVE: the client is responsible to provide callId, and whenever a new callId
       #          is provided for a room, the previous call is closed
      callIdAssignMode: master
    # obfuscator component settings
    obfuscator:
      # indicate if it is enabled or not
      enabled: false
      # indicate how the ICE addresses has been obfuscated.
      # possible values are: none, anonymization
      iceAddresses: none
      # indicate how the room ids are obfuscated.
      roomId: none
      # indicate how the user ids are obfuscated.
      userId: none

  # Setup the sinks to forward Reports.
  # Detailed description is in the Configuration section
  sinks: {}

  hazelcast:
    # custom config for hazelcast storage
    config:
      cluster-name: my-hz-cluster
    
```

#### Sinks

The observer forward the generated reports through sinks.
There are different type of sinks implemented to integrate different type of services.
A sink has the following configuration structure:

```yaml
sinks:
  # a unique key for a sink added to the configuration
  MyUniqueSinkKey:
    # The type of the sink. 
    # Each integration has its own sink type. 
    # for example for kafka the type of the sink KafkaSink
    type: SinkType # REQUIRED
    # A config passed to the instantiated SinkType
    config: {}
    # determines which type of reports are allowed to forward on this sinks
    # if it is not set then it allows all type of reports to send
    reports:
        sendObserverEvents: true
        sendCallEvents: true
        sendCallMeta: true
        sendClientExtensions: true
        sendInboundAudioTracks: true
        sendInboundVideoTracks: true
        sendOutboundAudioTracks: true
        sendOutboundVideoTracks: true
        sendClientTransports: true
        sendClientDataChannels: true
        sendMediaTracks: true
        sendSfuEvents: true
        sendSfuMeta: true
        sendSfuTransports: true
        sendSfuSctpStreams: true
        sendSfuOutboundRtpStreams: true
        sendSfuInboundRtpStreams: true
    
```

##### KafkaSink

Observer can send reports to [Apache Kafka](https://kafka.apache.org/). by using `KafkaSink`

```yaml
sinks:
    MyKafkaSink:
        type: KafkaSink
        config:
          # indicate if the Sink tries to reconnect in case of failure or not
          tryReconnectOnFailure: True
          # the name of the kafka topic reports are sent to 
          topic: "reports"
          # the strategy to assign keys for Kafka entries. 
          # This is important if you have multiple brokers and consumers from the same topic 
          # possible values are:
          #  * RANDOM - keys are randomly distributed
          #  * INSTANCE_BASED - reports from the same observer will have the same key
          #  * OBJECT_HIERACHY_BASED - reports belongs to the same call having the same key. 
          #                           if callId is not available then clientId or sfuId is used 
          keyAssignmentStrategy: INSTANCE_BASED
          # properties of the created kafka producer.
          # More info on settings: (https://kafka.apache.org/documentation/#producerconfigs).
          properties:
            bootstrap.servers: localhost:9092
```

##### MongoSink

Observer can send reports to  [Mongo Database](https://www.mongodb.com/). by using `MongoSink`

```yaml
sinks:
  MyMongoSink:
    type: MongoSink
    config:
      # the URI used to connect to mongo
      uri: "mongodb://root:password@localhost:27017"
      # the name of the database the Reports are written to
      database: "reports"
      # the strategy to save the reports
      # Possible values are:
      # * REPORT_TYPE_BASED - different type of reports are written to different collections
      # * SERVICE_ID_BASED - reports have different serviceId are written to different collections
      # * ONE_COLLECTION - every report is written into one collection
      savingStrategy: ONE_COLLECTION
      # print the summary to the console each time a batch of writing has been finished
      printSummary: True
```

##### LoggerSink

Observer can print reports to the console by using `LoggerSink`

```yaml
sinks:
  MyMongoSink:
    type: LoggerSink
    config:
      # the level of the logger used to print information to the console
      logLevel: INFO
      # prints the received reports to the console 
      printReports: True # default is False
      # prints the summary of the type of reports received by the sink
      printTypeSummary: False
```


## Performance

TBD

## Compatibility

Observer versions and compatible [schema](https://github.com/ObserveRTC/schemas) versions

| schemas → <br/>observer ↓ | 2.0.0              |   |   |   |
|---------------------------|--------------------|---|---|---|
| 1.0.0                     | :white_check_mark: |   |   |   |



## Getting involved

Observer is made with the intention to provide a scalable open-source monitoring solution for 
WebRTC developers. We develop new features and maintaining the current 
product with the help of the community. If you are interested in getting involved 
please read our [contribution](CONTRIBUTING.md) guideline.

## Changelog

[Changelog](CHANGELOG.md)

## License

[Apache 2.0](LICENSE)










