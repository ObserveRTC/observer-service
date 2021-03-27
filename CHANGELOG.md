All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.

## 0.7.3 (H)

### Added
 * Add Jackson Encoder to the encoder type, in order to transform avro reports to json format
 * Add `messageFormat` field to the LoggerSink configuration to support printing and accepting json type encoded messages.
 * Added exposed metric `call_durations_in_mins` reports the duration of an ended call at the time it is ended
 * Add marker field to the PeerConnectionSample schema
 * Added new address for pc samples `/pcsamples/{serviceUUID}/{mediaUnitId}`


## 0.7.2 (H)

### Added
 * Websocket URL address /pcsamples/{serviceUUID}/{mediaUnitId}/
 * `SourceSample` class can serve as a general intermediate object for different kind of source types
 * Configuration for sources.pcSamples 
 * `Sources` singletone object to funnel different type of sources
 * Encoders concept, to make different type of encoding for sink for reports possible
 * `AvroEncoder`, `AvroEncoderBuilder`, `EncoderBuilder` for encoding
 

### Deprecated
 * Websocket URL address /{serviceUUID}/{mediaUnitId}/v20200114/json
 * Configuration property security.dropUnknownServices as this option will belongs to the configurable sources. 

## 0.7.1 (H)

### Added
 * Sentinels checking peer connections distinguished from calls

### Removed
 * SSRCs attribute from callFiltersDTO

### Fixed
 * packetlost metric exposed by inbpundRTPMonitor now has positive values. 
 * Use Prototype instead of Singletone at SentinelMetricsProvider to have more than one sentinel

## 0.7.0 (H)

### Added

 * /callfilters endpoint (GET, PUT, DELETE, POST)
 * /pcfilters endpoint (GET, PUT, DELETE, POST)
 * /sentinels endpoint (GET, PUT, DELETE, POST) 
 * /servicemaps endpoint (GET, PUT, DELETE, POST)
 * Sentinels to watch calls periodically going through the pipeline and expose metrics.
 * Security improvements by adding configuration to drop unmapped samples.
 * /home endpoint for multiple OAuth2 support (Google, and AWS are added, maybe more comes)

### Deprecated
 - 

### Removed
 * All type of DTO added in `0.6.x`.
 * All previously created Task objects


### Fixed
 * Illmatched calls by fixed SSRCs without call names.
 * WeakLocks to handle timeouts
 * Fix null exception thrown when user media does not have pc uuid.
 * Fix null tag value in report counting.

### Refactored
 * `repositories` package to use `ChainedTasks` newly created object performing tasks in a transactional manner.
 * All DTO objects used to store in hazelcast in-memory data grid
