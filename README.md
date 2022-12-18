WebRTC-Observer
==

![Build](https://github.com/ObserveRTC/observer/actions/workflows/build.yml/badge.svg)
![Test](https://github.com/ObserveRTC/observer/actions/workflows/test.yml/badge.svg)

Observer is a server-side service for monitoring WebRTC applications. You can run it in a docker container with a command `docker run observertc/observer`. 
The service by default is listening on the port `7080`.

To see ObserveRTC integrations and examples, check out the [full stack examples repository](https://github.com/ObserveRTC/full-stack-examples).

### Table of Contents:
 * [Overview](#overview)
 * [Scalability](#scalability)
 * [Configurations](#configurations)
   * [Micronaut configs](#micronaut-configs)
   * [Management endpoints configs](#management-endpoints-configs)
   * [Observer configs](#observer-configs)
   * [Hamok configs](#hamok-configs)
 * [Sinks](#sinks)
   * [LoggerSink](#loggersink)
   * [Apache Kafka](#apache-kafka)
   * [Mongo Database](#mongo-database)
   * [AWS Firehose](#aws-firehose-sink)
   * [AWS S3 Bucket](#aws-s3-bucket-sink)
   * [AWS Credential Settings](#aws-credential-settings)
 * [Performance considerations](#performance-considerations)
 * [Compatibility](#compatibility)
 * [Getting Involved](#getting-involved)
 * [Changelog](#changelog)
 * [License](#license)


## Overview

![Overview](docs/images/superficial-overview.png)

Observer is a server-side service for monitoring WebRTC applications. The service receives [Samples](https://observertc.org/docs/overview/schemas/#samples)
from [Monitors](https://observertc.org/docs/overview/monitors/). Based on the received samples, the observer:

 * Identify calls
 * Create events (call started, call ended, client joined, client left, etc.)
 * Match clients' inbound-rtp sessions with remote clients' outbound-rtp sessions
 * Match SFUs RTP sessions with browser-side clients' in-, and outbound-rtp sessions.


### Receiving Samples

The observer accepts Samples on the following endpoints:

* **REST**: `http(s)://${OBSERVER_ADDRESS}:{PORT}/rest/samples/{serviceId}/{mediaUnitId}`
* **Websocket**: `ws(s)://${OBSERVER_ADDRESS}:{PORT}/samples/{serviceId}/{mediaUnitId}`

### Sending Reports

The observer create [Reports](https://observertc.org/docs/overview/schemas/#reports) based on the obtained events, matched sessions and received samples.
The reports are forwarded to [Sinks](#sinks).  Currently, the following type of sinks are supported:
* [KafkaSink](#kafkasink): Apache Kafka Integration
* [MongoSink](#mongosink): Mongo Database integration
* [FirehoseSink](#firehosesink): AWS Firehose integration
* [AwsS3Sink](#awss3sink): AWS S3 integration
* [WebsocketSink](#websocketsink): Websocket Sink

### Scalability

The service uses a distributed in-memory object storage [hamok](https://github.com/balazskreith/hamok) as its base to store information about calls while 
clients and SFUs are sending samples. If the service need to be scaled horizontally you need to setup
hamok in your configuration. Additionally, you can take a look at the section   
[hamok configurations](#hamok-configs) to have more information about the built in discovery and communication endpoint features.

Some useful tips you can find how to design your service in terms of traffic at the [performance consideration](#performance-considerations) section.

## Configurations

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
 * **hamok**:  defines the settings of the [hamok](https://github.com/balazskreith/hamok) distributed object storage the service uses.

Here is an example for the observer configuration part:

```yaml
observer:
   
   # configuration to expose metrics
   metrics:
      # the prefix for metrics exposed by the observer processes
      prefix: "observertc"
      # the name of the tag for serviceId used where it is exposed
      serviceIdTagName: "service"
      # the name of the tag for mediaUnitId used where it is exposed
      mediaUnitIdTagName: "mediaUnit"
      # metrics exposed from evaluators
      evaluatorMetrics:
         # flag indicate if evaluator metrics are exposed or not
         enabled: false
      reportMetrics:
         # flag indicate if report metrics are exposed or not
         enabled: false
      sinkMetrics:
         # flag indicate if sink metrics are exposed or not
         enabled: false
      sourceMetrics:
         # flag indicate if source metrics are exposed or not
         enabled: false
      repositoryMetrics:
         # flag indicate if repository metrics are exposed or not
         enabled: false
         # sets the period for exposing metrics about the number of entries the repository holds
         exposePeriodInMins: 5
         
  sources:
     # flag indicate if sfu sampels are accepted or not
    acceptSfuSamples: true
    # flag indicate if client samples are accepted or not
    acceptClientSamples: true
    # if it sets to true then observer overrides the samples provided timestamp to its own, to simplify the problem 
    # of different clients reporting timestamp in different time zones.
    useServerTimestamp: false
    # settings related to REST API accepting Samples
    rest:
      # indicate if Samples are accepted through REST or not
      enabled: true
    # settings related to Websockets accepting Samples
    websocket:
      # indicate if Samples are accepted through websockets or not
      enabled: true
      # this defines how many remote observer peers must be connected to the grid before 
      # the websocket starts accepting sessions
      # this configuration is useful to avoid a huge amount of session directed to the first 
      # available observer instance
      minRemotePeers: 0

  repository:
    # indicate if the repositories storages in hamok should use distributed backup service or not.
    # NOTE: Distributed backup service increases traffic inside the cluster and volume of data stored in the memory
    useBackups: false
    # the maximum idle time for a call object after which it is removed from the repository
    callMaxIdleTimeInS: 300
    # the maximum idle time for an SFU object after which it is removed from the repository
    sfuMaxIdleTimeInS: 300
    # the maximum idle time for a Sfu Transport object after which it is removed from the repository
    sfuTransportMaxIdleTimeInS: 600
    # the maximum idle time for a client object after which it is removed from the repository
    clientsMaxIdle: 300
    # the maximum idle time for a peer connection object after which it is removed from the repository
    peerConnectionsMaxIdle: 300
    # the maximum idle time for an inbound track object after which it is removed from the repository
    inboundTracksMaxIdle: 300
    # the maximum idle time for an outbound track object after which it is removed from the repository
    outboundTracksMaxIdle: 300
    # the maximum idle time for a sfu inbound rtp pad object after which it is removed from the repository
    sfuInboundRtpPadMaxIdleTimeInS: 300
    # the maximum idle time for a sfu outbound rtp pad object after which it is removed from the repository
    sfuOutboundRtpPadMaxIdleTimeInS: 300
    # the maximum idle time for a sfu sctp channel object after which it is removed from the repository
    sfuSctpChannelMaxIdleTimeInS: 300

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
     # settings for component analyse the samples from 
     # client applications
    clientSamplesAnalyser:
       # drop reports created from inbound-audio and video if 
       # their outbound pair has not been found  
       dropUnmatchedReports: false

    # settings for component analyse the samples from SFUs
    sfuSamplesAnalyser:
       # drop reports created from inboundRtpPad if their sfuStreamId
       # been not registered by a client
       dropUnmatchedInboundReports: false
       # drop reports created from outboundRtpPad if their sfuSInkId
       # been not registered by a client
       dropUnmatchedOutboundReports: false
       # drop reports created from internal inboundRtpPad samples if their counterpart
       # internal outbound rtp pads are not matched
       dropUnmatchedInternalInboundReports: false
    
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
  # Detailed description is in the Sinks section
  sinks: {}

  # settings related to hamok distributed object storage 
  # and discovery of other observer instances to create a grid
  hamok:
    # settings related to the storage grid the observer shares objects through
    storageGrid:
       # the retention time in minutes the Raft keeps logs for
       # this determines how long a new peer can join to the cluster without 
       # issuing a storage sync. If a peer joins after a retention time,
       # it issues a storage sync which requires the leader to synchronize all 
       # storage with the new follower
       raftMaxLogEntriesRetentionTimeInMinutes: 5
       
       # the period of the heartbeat for the leader.
       # this determines for example how fast the replicated storages get updates
       heartbeatInMs: 150
       
       # the timeout for a follower when it does not get a heartbeat message 
       # from a leader and start an election
       # rule of thumb to make it at least 3 or 5 times higher than the heartbeat + network delay
       followerMaxIdleInMs: 1000
       
       # maximum time for a request between storage grids to exchange
       # information. note that finding a balance for this value needs consideration 
       # of the network you put the instance in, and the resource capacity your running instance 
       # acquires. if it is too small and response time in the network too big it can cause false 
       # interpretation that the remote peer is not responding. However if it is too large 
       # then becasue of the blocking nature of storage operations this can cause an observer 
       # to throttle samples and reports
       requestTimeoutInMs: 3000
    
    # For other type of discovery settings, please take a look at hamok configuration section
    discovery:
       type: StaticDiscovery
       config:
          peers:
             my-other-peer:
                port: 5603
                host: "localhost"
                
    # For other type of endpoints for hamok to communicate please take a look at hamok cofiguration section
    endpoint: 
       # The type of the endpoint hamok uses to communicate
       type: WebsocketEndpoint
       config:
          # the hostname
          serverHost: "localhost"
          # the port number to offer a websocket connection
          serverPort: 5600
          
          # Discovery strategy to discover another endpoint
          
```

### Hamok configs

Hamok is a distributed in-memory object storage. The configuration in observer to setup the property 
of the storageGrid, providing local-, and to discover remote endpoints. 
```yaml
observer:
  hamok:
    storageGrid: {}
    discovery: {}
    endpoint: {}
```
Currently the following endpoints can be made in observer for hamok:
 * [WebsocketEndpoint](#websocket-endpoint-settings)

Currently the following discovery settings can be made in observer for hamok:
 * [StaticDiscovery](#static-discovery-settings)
 * [KubernetesDiscovery](#kubernetes-discovery-settings)
 
#### Websocket Endpoint Settings

```yaml
endpoint: {}
    # The type of the endpoint hamok uses to communicate
    type: WebsocketEndpoint
    config:
       # the hostname
       serverHost: "localhost"
       # the port number to offer a websocket connection
       serverPort: 5600
```

#### Static Discovery Settings

```yaml
 discovery:
    type: StaticDiscovery
    config:
       peers:
          my-other-wonderful-peer:
             port: 5603
             host: "localhost"
```
#### Kubernetes Discovery Settings

```yaml
 discovery:
    type: K8sPodsDiscovery
    config:
       # The namespace where the instance is runninng in
       namespace: "default"
       
       # prefix for the service the pods are installed by
       prefix: "observertc"
       
       # the port number on which the remote peer accepts a connection to
       port: 5601# 
```

## Sinks

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
        sendPeerConnectionTransports: true
        sendIceCanddiatePairs: true
        sendClientDataChannels: true
        sendSfuEvents: true
        sendSfuMeta: true
        sendSfuTransports: true
        sendSfuSctpStreams: true
        sendSfuOutboundRtpStreams: true
        sendSfuInboundRtpStreams: true
        sendSfuExtensions: true
    
```

### LoggerSink

Observer can print reports to the console or to the configured logback output by using `LoggerSink`

```yaml
sinks:
  MyLoggerSink:
    type: LoggerSink
    config:
      # the level of the logger used to print information to the console
      logLevel: INFO
      # prints the received reports to the console 
      printReports: True # default is False
      # prints the summary of the type of reports received by the sink
      printTypeSummary: False
```

### Apache Kafka

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

### Mongo Database

Observer can send reports to  [Mongo Database](https://www.mongodb.com/). by using `MongoSink`

```yaml
sinks:
  MyMongoSink:
    type: MongoSink
    config:
      # the URI used to connect to mongo. either this or the connection must be provided
      uri: "mongodb://root:password@localhost:27017"
      # a config object describes the uri. either this or the uri must be given
      connection:
        # the username for mongodb
        username: "admin"
        # the password for mongodb
        password: "password"
          # list of mongodb servers
          servers:
            - "localhost:27017"
      
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

### AWS Firehose Sink

Observer can send reports to [Aws Firehose](https://aws.amazon.com/kinesis/data-firehose/) via DIRECT PUT method.

```yaml
sinks:
   MyFireHoseSink:
      type: FirehoseSink
      config:
         # The encoding format of the forwarded data. Possible values are: JSON, CSV
         # For csv format, please check the schemas repository for the headers
         encodingType: CSV
         # The AWS region the firehose has been configured
         regionId: eu-west-1
         # the name of the delivery stream the records are forwarded by default
         defaultDeliveryStreamId: observertc-reports-csv
         # map report types to delivery streams records are forwarded to
         # Optional. 
         # if both streams and the defaultDeliveryStreamId are provided than the report forwarded to the default if it is not mapped
         streams:
            observer-event: observertc-observer-event-reports-csv
            call-event: observertc-call-event-reports-csv
            call-meta-data: observertc-call-meta-data-reports-csv
            client-extension-data: observertc-client-extension-data-reports-csv
            peer-connection-transport: observertc-peer-connection-transport-reports-csv
            ice-candidate-pair: observertc-ice-candidate-pair-reports-csv
            peer-connection-data-channel: observertc-peer-connection-data-channel-reports-csv
            inbound-audio-track: observertc-inbound-audio-track-reports-csv
            inbound-video-track: observertc-inbound-video-track-reports-csv
            outbound-autio-track: observertc-outbound-autio-track-reports-csv
            outbound-video-track: observertc-outbound-video-track-reports-csv
            sfu-event: observertc-sfu-event-reports-csv
            sfu-meta-data: observertc-sfu-meta-data-reports-csv
            sfu-extension-data: observertc-sfu-extension-data-reports-csv
            sfu-transport: observertc-sfu-transport-reports-csv
            sfu-inbound-rtp-pad: observertc-sfu-inbound-rtp-pad-reports-csv
            sfu-outbound-rtp-pad: observertc-sfu-outbound-rtp-pad-reports-csv
            sfu-sctp-stream: observertc-sfu-sctp-stream-reports-csv

         # A provided credential to access AWS resources
         # see detailed description in Credentials section
         # To configure credentials for AWS services, 
         # please look at the AWS Credential Settings section in the readme.
         credentials:
            type: AwsStatic
            config:
               accessKeyId: <ACCESS_KEY>
               secretAccessKey: <SECRET>

         # in case CSV encoding is used, this instructs the CSV format written into the records
         # possible values are: DEFAULT, EXCEL, INFORMIX_UNLOAD, INFORMIX_UNLOAD_CSV, MONGODB_CSV, MONGODB_TSV, MYSQL, ORACLE,
         # POSTGRESQL_CSV, POSTGRESQL_TEXT, RFC4180
         # by default it is DEFAULT
         csvFormat: DEFAULT
         # The number of reports put into one Firehose PUT request.
         # default is 100
         csvChunkSize: 100
```

To configure credentials for AWS services, please look at the [AwsCredentials Configuration](#aws-credentials-configuration) section.


### AWS S3 Bucket Sink

Observer can send reports to [Aws S3](https://aws.amazon.com/s3/) via REST requests

```yaml
sinks:
  MyAwsS3Sink:
    type: AwsS3Sink
    config:
       # The encoding format of the forwarded data. Possible values are: JSON, CSV
       # For csv format, please check the schemas repository for the headers
       encodingType: CSV
       # the name of the bucket observer upload the reports to
       bucketName: "my-bucket"
       # The AWS region the firehose has been configured
       regionId: eu-west-1
       # the name of the default prefix used if no other prefix defined
       defaultPrefix: observertc-reports-csv
       # flag indicate to add serviceId prefix for the uploaded reports
       addServiceIdPrefix: false
       # flag indicate to add reports category (call-reports / sfu-reports) prefix for the uploaded reports
       addReportCategoryPrefix: false
       # flag indicate to add sfu or call id prefix for the uploaded reports
       addSfuOrCallIdPrefix: false
       # prefix used to upload objects to s3
       prefixes:
          observer-event: observertc-observer-event-reports-csv
          call-event: observertc-call-event-reports-csv
          call-meta-data: observertc-call-meta-data-reports-csv
          client-extension-data: observertc-client-extension-data-reports-csv
          peer-connection-transport: observertc-peer-connection-transport-reports-csv
          ice-candidate-pair: observertc-ice-candidate-pair-reports-csv
          peer-connection-data-channel: observertc-peer-connection-data-channel-reports-csv
          inbound-audio-track: observertc-inbound-audio-track-reports-csv
          inbound-video-track: observertc-inbound-video-track-reports-csv
          outbound-autio-track: observertc-outbound-autio-track-reports-csv
          outbound-video-track: observertc-outbound-video-track-reports-csv
          sfu-event: observertc-sfu-event-reports-csv
          sfu-meta-data: observertc-sfu-meta-data-reports-csv
          sfu-extension-data: observertc-sfu-extension-data-reports-csv
          sfu-transport: observertc-sfu-transport-reports-csv
          sfu-inbound-rtp-pad: observertc-sfu-inbound-rtp-pad-reports-csv
          sfu-outbound-rtp-pad: observertc-sfu-outbound-rtp-pad-reports-csv
          sfu-sctp-stream: observertc-sfu-sctp-stream-reports-csv

       # A provided credential to access AWS resources
       # see detailed description in Credentials section
       # To configure credentials for AWS services, 
       # please look at the AWS Credential Settings section in the readme.
       credentials:
          type: AwsStatic
          config:
             accessKeyId: <ACCESS_KEY>
             secretAccessKey: <SECRET>
          
       # in case CSV encoding is used, this instructs the CSV format written into the records
       # possible values are: DEFAULT, EXCEL, INFORMIX_UNLOAD, INFORMIX_UNLOAD_CSV, MONGODB_CSV, MONGODB_TSV, MYSQL, ORACLE,
       # POSTGRESQL_CSV, POSTGRESQL_TEXT, RFC4180
       # by default it is DEFAULT
       csvFormat: DEFAULT
```

To configure credentials for AWS services, please look at the [AwsCredentials Configuration](#aws-credentials-configuration) section.


### AWS Credential Settings

AWS services require providing credentials. The credential configuration is part of the actual sink configuration
setting up to connect to an AWS service.
For example Firehose or AwsS3 sink have the `credentials` property in their configurations.

Possible credential types and configuration structures are the followings:
 * [AWS Profile credential settings](#aws-profile-credential-settings) 
 * [AWS Environment credential settings](#aws-environment-credential-settings)
 * [AWS STS Assume Role Session Settings](#aws-sts-assume-role-session-settings)
 * [AWS Static Credential Settings](#aws-static-credential-settings)
 * [AWS System Credential Settings](#aws-system-credential-settings)
 * [AWS Web Identity Token Credential Settings](#aws-web-identity-token-credential-settings)

#### AWS Profile credential settings

File based profile credentials

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      credentials:
         type: AwsProfile
         config:
           profileFilePath: /my/path/to/credential/file
           # possible values are CONFIGURATION, and CREDENTIALS. default CREDENTIALS
           profileFileType: CREDENTIALS
           # the name of the profile fetched for credentials
           profileName: myprofileName
```

#### AWS Environment credential settings

Loads credentials from the AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY and AWS_SESSION_TOKEN environment variables

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      credentials:
         type: AwsEnvironmentVariable
```

#### AWS STS Assume Role Session Settings

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      credentials:
         type: AwsStsAssumeRoleSession
         config:
            # the region id of the assumed role
            regionId: eu-west-1
            # the ARN of the role
            roleArn: "THE_ARN_OF_THE_ROLE"
            roleSessionName: "observer"
         
            # give credentials to the STS client accessing to the assumed role
            # if the client requires different credentials than the currently provided one.
            # the settings of the credential here is the same as to any AWS credential listed here
            credentials: # optional 
               type: AwsStatic
               config:
                  accessKeyId: accessKey
                  secretAccessKey: secret
            serialNumber: "1234-5678" # optional
            policy: "policy" # optional
            externalId: "external" # optional
```

#### AWS Static Credential Settings

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      credentials:
         type: AwsStatic
         config:
            accessKeyId: accessKey
            secretAccessKey: secret
```

#### AWS System Credential Settings

Loads credentials from the aws.accessKeyId, aws.secretAccessKey and aws.sessionToken system properties.

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      credentials:
        type: AwsSystemProperty
```

#### AWS Web Identity token credential settings

A credential provider that will read web identity token file path, 
aws role arn and aws session name from system 
properties or environment variables for using web 
identity token credentials with STS. Use of this credentials 
provider requires the 'sts' module to be on the classpath.

```yaml
MyAwsS3Sink:
   type: AwsS3Sink
   config:
      type: AwsWebIdentityTokenFile
      config:
        roleArn: roleArn
        roleSessionName: roleSessionName
```

## Performance considerations

Observer performance is highly influenced by two factors: how many clients providing samples at the same time, 
and how much time it takes to forward the reports to the configured sink(s). The performance can be tuned by settings the buffers.
For example higher threshold for samples making larger batches to process and send to the sinks, giving more time to the sinks 
to forward the previous batch, but also it can make larger spikes in memory as tens or hundreds of thousands of reports 
are going to be throttled to the sinks waiting to forward. 

Though it is very hard to give an exact number of how many clients can be connected to one observer instance and 
how much memory and CPU should be given to one instance, to start with you can use the following may help in your calculations:

![Performance Box](docs/images/performance-box.png)

Assuming the majority of calls (80%) are less than 4 participants, then 1000 active session can produce around 30 Samples per sec
(if the sampling period is around 10s for each client) then 2GB of memory should / could handle 1000 active session at the same time assuming, 
producing 1000 reports per sec to the sinks to handle.

Anothe case if you design a service for 5000 active sessions at any time, then either you scale it vertically to have bigger CPU and around 10GB memory,
or you can scale horizontally and start running 5 instances with the same configurations.  

It must be noted that this estimation is superficial and based on experiments and experience we have so far.

In terms of horizontal scalability it must be noted too, that since the service uses in-memory distributed storage memory consumption is 
crucial and an instance cannot / should not run out of memory. 


## Compatibility

Observer versions and compatible [schema](https://github.com/ObserveRTC/schemas) versions

| schemas → <br/>observer ↓ | 2.0.y              |   |   |   |
|---------------------------|--------------------|---|---|---|
| 1.0.x                     | :white_check_mark: |   |   |   |


## Getting involved

Observer is made with the intention to provide a scalable open-source monitoring solution for 
WebRTC developers. We develop new features and maintaining the current 
product with the help of the community. If you are interested in getting involved 
please read our [contribution](CONTRIBUTING.md) guideline.

## Changelog

[Changelog](CHANGELOG.md)

## License

[Apache 2.0](LICENSE)










