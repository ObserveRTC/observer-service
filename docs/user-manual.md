
![Overview](images/superficial-overview.png)

Observer consumes statistics generated from WebRTC end points like browsers and Selective Forwarding Units (SFUs).

In our terminology the provided stats object at a specific time is called a sample.
[Samples](/docs/schemas/samples/) are the input of the Observer.
Currently the following type of Samples are supported:
 * [ClientSample](/docs/schemas/client-sample/) for WebRTC client applications running in the browser
 * [SfuSample](/docs/schemas/sfu-sample/) for Selective Forwarding Units running at the server side.

Based on the received samples the Observer do the following:
 * Matching samples belong to the same call (see definition later)
 * Matching samples from SFUs with samples from clients
 * Matching video or audio streams between the source, and remote clients
 * Identify events based on the sample (call started, client joined, peer connection opened, etc...)
 * Assemble and send reports

The observer generates reports as output. [Reports]() compound and contain
relevant part of the sample based on the type of the report.
Currently the following type of Reports are assembled:
 * [CallEventReport](): Events related to calls (call started, call ended, client joined, client left, etc...)
 * [CallMetaDataReport](): Additional information the related stats provide (SDP negotiations, ICE connectivity, etc...)
 * [ClientDataChannelReport](): Measurements related to Data channel streams corresponded to a call.
 * [ClientExtensionReport](): Additional information the client application provide.
 * [ClientTransportReport](): Measurements of peer connection transport streams.
 * [InboundAudioTrackReport](): Measurements of inbound RTP related audio tracks.
 * [InboundVideoTrackReport](): Measurements of inbound RTP related video tracks.
 * [OutboundAudioTrackReport](): Measurements of outbound RTP related audio tracks.
 * [OutboundVideoTrackReport](): Measurements of outbound RTP related video tracks.
 * [ObserverEventReport](): Custom observer related events (basic aggregations provide by the service)
 * [SfuEventReport](): Events of SFUs (SFU joined, SFU detached, Transport opened, Transport closed, etc...)
 * [SfuMetaReport](): Additional information the related stats provide (SDP negotiations, etc...)
 * [SfuInboundRtpPadReport](): Measurements of inbound RTP streams.
 * [SfuOutboundRtpPadReport]():  Measurements of outbound RTP streams.
 * [SfuSctpStreamReport](): Measurements of SCTP streams.
 * [SfuTransportReport](): Measurements of Transport objects.


## Architecture

![Architecture Overview](images/architecture.png)


### Object Hierarchy

![Object-Hierarchy](images/object-hierarchy.png)

Observer analyze incoming samples and build an internal object hierarchy stores in its repository.
Object hierarchy is then used to group the client samples, matching tracks and sfu rtp pads,
identify tracks' peer connections, SFU transports, etc... The identifiers
revealed by the object hierarchy for the sample are assigned to the generated reports.
As a result, for example, all client related reports can be identified which call it belongs to.

The basic elements of the object hierarchy are the following:

 * **Service** is the top level object identify the owner the provided media service belongs to.
 * **Room** is a virtual place clients share to exchange media contents.
   Clients are in the same Room under the same Service participating in the same call.
 * **Call** identifies a group of clients exchange media contents at the same time in the same room under the same service.
 * **MediaUnit** hold information clients and SFUs can arbitrary add in the samples.
   For example, it can be the webrtc app version in the client side,
   and the geographic region and availability zone in the SFUs side.
 * **Client** uses the WebRTC app provides the ClientSamples
 * **PeerConnection**s are established by the client to another client or to an SFU.
 * **MediaTrack**s are used by the peer connection to send out or receive in media contents.
 * **SFU** or Selective Forwarding Units are server side components to compound and distribute media contents between
   several participants.
 * **SfuTransport** are the peer connection established by the SFU with either a client or with another SFU / media service.
 * **SfuRtpPad**s are used by the SfuTransports to send out or receive in media contents using RTP transport protocol.


## Use Cases

Let's make an example of how to use observer by following a story. In the example a company named `MyAwesomeTutorialForEveryoneExample` creates a webrtc based media service to provide tutorials all over the world.
The company develop a webrtc app called `my-webrtc-app`, and it is integrated to send samples to an observer.

### Observe peer to peer calls

The company provides `free-tutorials` media service through `my-webrtc-app` clients are actively using.
The `my-webrtc-app` send [ClientSample]()s to the observer using a websocket endpoint `wss://observer.MyAwesomeTutorialForEveryoneExample.com/free-tutorials/my-webrtc-app-1-0-0`, where the `free-tutorials` is the serviceId, and `my-webrtc-app-1-0-0` is the webrtc app bundled with its current version.

At 8am until 9am Alice and Bob enter to `cryptography` room using the link `https://www.MyAwesomeTutorialForEveryoneExample.com/cryptography`. While they are in the room `cryptography` observer collects the samples from both client and the generated reports forwarded by the observer contains the same callId. Furthermore Alice's outbound video and audio tracks are matched with Bob's inbound audio and video tracks from Alice, and the generated report contains the trackId which remote client id and track the inbound track belongs to.

### Observe multi-tenant calls

Your company wants to provide tutorials for a group of people and not just 1:1 sessions. You setup an SFU, with what your comapny is able to do a call with multiple participant. With no changes to the ClientSample creation setup for the p2p calls, the observer is able to match calls in the same room under the same service, but to match the tracks ClientSample Outbound-, and Inbound Audio, and Video calls must set the `rtpStreamId` UUID to match between tracks. this id is provided by the SFU client SDK and you should get it through a signal server. When this information is added observer can match the tracks just before to the p2p calls.


### Observe SFUs

Further going with monitoring, observer is able to accept samples from SFUs integrated to provide [SfuSample]()s. When an SFU sending samples to the observer, the observer is able to match SFU provided samples with client samples and generated reports for the SFU contains the callId an SfuRtpPad is opened for (using the matching `rtpStreamId`).

### Observe multiple services

Your company wants to provide paid tutorials for clients but using the same `my-webrtc-app`. In this case the only difference is the serviceId for the observer reported through the `my-webrtc-app`, which should be different than the previously used `free-tutorials`.

## Configurations

![Configuration Overview](images/configuration-overview.png)

The default configuration file is packed with the compiled observer in its resources, depending on the version
you are using the default configuration can be found [here]().
The application can be configured through its configuration file fetched at startup. To fetch a custom
configuration override the default values you need to set the `MICRONAUT_CONFIG_FILES` and point to your custom
configuration.

The structure of configuration is the following:

```yaml
micronaut: {}
---
observer:
    sources: {}
    evaluators: {}
    sinks: {}
    repository: {}
    hazelcast: {}
    buffers: {}
```

The application ses the [micronaut]() framework to provide the application capabilities.
For example under the **server** component in the configuration you can set yhe port the application listens
for incoming requests. The full documentation of the configuration related to micronaut framework can be found [here](https://docs.micronaut.io/latest/guide/configurationreference.html).

Using micronaut framework the observer can be configured by the following components:

Observer functionality can be separated into four main components: Sources, Sinks, Evaluators, and Repository.
 * **sources** are responsible to provide an interface to accept incoming samples.
 * **sinks** are responsible to connect to a service in order to send reports.
 * **evaluators** are responsible to analyze the incoming samples, execute necessary tasks to update the repository, and generate reports forwarded to sinks.
 * **repository** provide direct access to the observer storage system and also provide tasks to execute idempotent operations.
 * **hazelcast**: is responsible to provide storage capability for the repository. Observer uses [hazelcast]() IMDG to store its object hierarchy.
 * **buffers** are responsible to collect internal objects tune batch processing capabilities

### Sources

Sources define the connection where the samples are received from.

```yaml
sources:
    restapi: {}
    websockets: {}
```

Currently supported source types:
 * **REST API** endpoints accept batch of samples through http REST requests.
 * **Websockets** endpoint accepts samples through websocket connections

#### REST API


The REST API endpoints accept batch of samples through POST requests.
 * Accepting [ClientSample]()s on `https://{HOST_ADDRESS}:{HOST_PORT}/rest/clientsamples/{serviceId}/{mediaUnitId}`
 * Accepting [SfuSample]()s on `https://{HOST_ADDRESS}:{HOST_PORT}/rest/sfusamples/{serviceId}/{mediaUnitId}`
 * Accepting [Samples]() on `https://{HOST_ADDRESS}:{HOST_PORT}/rest/samples/{serviceId}/{mediaUnitId}`

The configuration to control the endpoints:

```yaml
sources:
    restapi:
        acceptClientSamples: true
        acceptSfuSamples: true
        maxClientSamplesBatch: 100
        maxSfuSamplesBatch: 100
```

where
 * `acceptClientSamples` indicate if the clientsample endpoint accept samples or not
 * `acceptSfuSamples` indicate if the sfusample endpoint accept samples or not
 * `maxClientSamplesBatch` determines what is the maximum size of batch one request can send for the clientsample endpoint
 * `maxSfuSamplesBatch` determines what is the maximum size of batch one request can send for the sfusample endpoint


#### Websockets

The observer have the following websocket endpoints:
 * Accepting [Samples](https://observertc.org/docs/schemas/samples/)s on `https://{HOST_ADDRESS}:{HOST_PORT}/samples/{serviceId}/{mediaUnitId}`


The configuration to control the endpoints:

```yaml
sources:
    websockets:
        enabled: true
        maxClientSamplesBatch: 100
        maxSfuSamplesBatch: 100

```

where
 * `enabled` indicate if the websocket connection can be established or not
 * `acceptSfuSamples` indicate if the sfusample endpoint accept samples or not
 * `maxClientSamplesBatch` determines what is the maximum size of batch one message can send
 * `maxSfuSamplesBatch` determines what is the maximum size of batch one message can send


### Evalautors

Evaluators configurations sets up parameters influence the behaviour of analyzing the incoming samples

```yaml
evaluators:
    reportSfuRtpPadWithCallIdTimeoutInS: 60 # default
```

where
 * `reportSfuRtpPadWithCallIdTimeoutInS` sets up the maximum waiting time to match an [SfuRtpPad]() with a call. If the timeout is passed the SfuRtpPad is reported to be added without the callId.

### Sinks

Sinks define the connection where the reports are sent to.
Each sink is under `sinks` configuration component and have the following structure:
```yaml
sinks:
    MyUniqueSinkId_1:
        type: SinkType
        config: {}
```

In the above example `MyUniqueSinkId_1` is the key in the config map under the sink. The `type` determines the client integration,
for which the appropriate configuration is fetched from `config`.
Currently supported sink types:
 * [KafkaSink]()
 * [MongoSink]()
 * [SocketIOSink]()
 * LoggerSink

#### KafkaSink

`KafkaSink` integrates [Apache Kafka]() so the observer can forward reports to kafka by setting up a sink as follows:

```yaml
sinks:
    MyKafkaSink:
        type: KafkaSink
        config:
            tryReconnectOnFailure: true # default value
            demuxTopicPrefix: "prefix-"
            properties:
                bootstrap.servers: localhost:9092
```

Under the `config` you can set:
    * `tryReconnectOnFailure`: Makes the sink resilient to failure of the kafka consumer, and tries to reconnect maximum 3 times.
    * `demuxTopicPrefix`: By default the KafkaSink sends each report type to a different topic, for which the topic name is
    built up as follows: `demuxTopicPrefix + [REPORT_TYPE]`.
    * `properties` is the config key to setup the properties of a [kafka producer](https://kafka.apache.org/documentation/#producerconfigs).

By default the KafkaSink send each report type to a different topic. That behavior can be altered and order the sink
to send all reports into one by multiplexing the reports.

```yaml
sinks:
    MyMuxedKafkaSink:
        type: KafkaSink
     config:
        muxReports: true # the default is false
        muxTopic: "observertc-reports"
        properties:
                bootstrap.servers: localhost:9092
```

#### MongoSink

`MongoSink` integrates [Mongo Database]() so the observer can forward reports to mongo by setting up a sink as follows:

```yaml
sinks:
    MyMongoSink:
        type: MongoSink
        config:
            uri: "mongodb://root:password@localhost:27017"
            database: "reports"
            collectionNames: {} # default is null

```

Under the `config` you can set:
    * `uri`: The URI of the mongo database
    * `database`: the name of the database inside mongo the sink write the reports to
    * `collectionNames` by default collection name for each report type is the name of the report type, but it can be overridden as follows:
        ```yaml
            collectionNames:
                INBOUND_AUDIO_TRACK: "myCollectionName"
        ```

#### SocketIOSink


`SocketIOSink` integrates [socket-io]() network connection between the observer and the socket-io server by setting up a sink as follows:

```yaml
sinks:
   MySocketIO:
    type: SocketIOSink
    config:
        uri: "http://localhost:1234"
        timeoutInMs: 500
        reconnectionDelayInMs: 1000 # default
        maxRetry: 3 # default
        transports:
         - websocket # default
         - polling


```

Under the `config` you can set:
    * `uri`: The URI of the server accepts [socket-io]() type connections
    * `timeoutInMs`: Connection timeout (ms). Set -1 to disable
    * `reconnectionDelayInMs`:  The delay in ms before the client tries to reconnect to the server
    * `maxRetry`: the maximum number of retry
    * `transorts`: the transport protocols the client tries to establish to the server. possible values are: websocket, and polling


#### LoggerSink


`LoggerSink` is a sink writes to the console and mainly used for troubleshooting purposes. It can be set up as follows:

```yaml
sinks:
    MyLoggerSink:
        type: LoggerSink
        config:
            printTypeSummary: False # default is True
            printReports: True # default is False
            logLevel: WARN # default is INFO
```

Under the `config` you can set:
    * `printTypeSummary`: Indicate if a summary about the generated number of reports by type should be printed or not
    * `printReports`: Indicate to print each generated report to the console
    * `logLevel` determines the level of the console log (useful for instance if the default log level of the application is higher than INFO, then in order to see the console logs written by this sink you need to set the level higher)


### Repository

Repository config influences the lifetime of the object stored to ints internal repository

```yaml
repository:
    mediaTracksMaxIdleTimeInS: 300
    peerConnectionsMaxIdleTime: 300
    clientMaxIdleTimeInS: 300
    sfuRtpPadMaxIdleTimeInS: 600
    sfuTransportMaxIdleTimeInS: 600
    sfuMaxIdleTimeInS: 60
```

where
* `mediaTracksMaxIdleTimeInS` determines the maximum idle time (no new sample received to update that track) for a single media track object after which it is removed from the repository
* `peerConnectionsMaxIdleTime` determines the maximum idle time (no new sample received to update that track) for a singlepeer connection object after which it is removed from the repository together with all media track it had
* `clientMaxIdleTimeInS` determines the maximum idle time (no new sample received to update that track) for a single client object after which it is removed from the repository together with all peer connection it had
* `sfuRtpPadMaxIdleTimeInS` determines the maximum idle time (no new sample received to update that track) for a single sfu rtp pad object after which it is removed from the repository
* `sfuTransportMaxIdleTimeInS` determines the maximum idle time (no new sample received to update that track) for a single sfu transport object after which it is removed from the repository together with all rtp pads it had
* `sfuMaxIdleTimeInS` determines the maximum idle time (no new sample received to update that track) for a single sfu object after which it is removed from the repository together with all transports it had



### Hazelcast

The repository observer uses is built on using [Hazelcast](). Hazelcast configuration is used to spread observer internal memory storage and spread accross all observer running in one cluster.

```yaml
hazelcast:
    configFile: classpath:hazelcast.yaml # default
    logs: []
    memberNamesPool: []
```

where
 * `configFile` sets the config file hazelcast instance is used to bootstrap.
 * `logs` holds an array of logging level printed out into the console catched from hazelcast. possible values are: ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE
 * `memberNamesPool` holds an array of value observer assign a name from in order to give in the logs a human readable name instead of the UUID hazelcast gives.

### Buffers

Observer uses internal buffers to collect, debounce and emit events or objects. The internal buffer configuration determines the delay between a sample is received and reports are forwarded. The shorter the buffer time limitation the faster observer generate reports, however observer is written to execute on batch of events and more efficient in the aspect of performance to process larger batches the memory respecting the memory limitations a vm instance is running hs.

```yaml
buffers:
    repositoryEventsDebouncers: {}
    clientSamples: {}
    sfuSamples: {}
    clientProcessDebouncers: {}
    sfuProcessDebouncers: {}
    outboundReports: {}
```

`repositoryEventsDebouncers`, `clientSamples`, `sfuSamples`, `clientProcessDebouncers`, `sfuProcessDebouncers`, and `outboundReports` have the following structure:

```yaml
    maxItems: 100
    maxTimeInS: 3
```

where `maxItems` determines the maximum number of item the buffer can hold, and the `maxTimeInS` determines the threshold for time in seconds a buffer holds items. Either option can be `0`, which means it skip limitation of time or item number, but not both!


## Samples and Reports (I/O)

## Troubleshooting

## Appendixes

### ReportTypes

### CallEventTypes

### SfuEventTypes

### Level of logs



