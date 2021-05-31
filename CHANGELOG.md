All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.

## 0.8.2 (H)
* Secure websocket connections

### Added
 * Added `security` package targeting security concerns for observer
 * Added Websocket security through accessToken part of the request the websocket sending.
 * Added configuration `observer.security.websockets.expirationInMin` for explicit expiration of websocket access tokens

### Bugfix
 * ExtensionStats Report was not made. It is fixed now.

## 0.8.1 (H)
 * Make `/config` endpoint to rule all possible runtime configuration changes
 * Add AsciiArt logo to print at startup (most important!)

### Added
 * New endpoint: `/config` to change any configuration
 * ObserverConfigDispatcher to dispatch config changes runtime
 * ConfigEntriesDispatcher to listen config repository changes in Hazelcast
 * ConfigDTO to store configurations in hazelcast repository
 * `configs` package to bundle all config structures, stores and dispatchers
 * `ConfigOperations` to detect changes in a config map
 * `ConfigNode` to help identifying Config map nodes
 * `ConfigHolder` to hold configurations and renew it accordingly

### Removed
 * `/pcFilters` endpoint is removed due to `/config` endpoint
 * `/callFilters` endpoint is removed due to `/config` endpoint
 * `/sentinels` endpoint is removed due to `/config` endpoint
 * `/servicemappings` endpoint is removed due to `/config` endpoint
 
### Changed
 * SentinelsDTO to SentinelConfig as it become part of the config
 * CallFilterDTO is removed from Hazelcast repository, and become CallFilterConfig as it is part of a config object
 * PeerConnectionFilterDTO from Hazelcast repository, and become PeerConnectionFilterConfig as it is part of a config object
 * ServiceMappings from Hazelcast repository, and become part of the config object
 * CollectionFilterDTO from Hazelcast repository, and become CollectionFilterConfig as it is part of a config object
 * Connectors Can be changed dynamically through `/config` endpoint in runtime

## 0.8.0 (H)
 * Add `framewidth`, and `frameHeight` fields to tracks.
 * Change Report schema

## 0.7.3 (H)

### Added
 * Add Json Encoder to the encoder type, in order to transform avro reports to json format
 * Add `format` to encoder config to indicate the output format (`bytes`, `object`) depending on the sink requirements
 * Added exposed metric `call_durations_in_mins` reports the duration of an ended call at the time it is ended
 * Add marker field to the PeerConnectionSample schema
 * Added new address for pc samples `/pcsamples/{serviceUUID}/{mediaUnitId}`
 * Add Bson Encoder to the encoder type
 * Add MongoSink to the sinks
 * Change the documentation to asciidoc
 * Add Obfuscator transfromations

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
