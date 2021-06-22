All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.

## 1.0.0 (H)

Breaking Changes:
 * Using new Schema for reporting (Reports v3)
 * Using new Schema to accepting samples (ClientSample)
 * 

Plan:
 * Remove KeyMaker from configholds
 * Clear separation what is static config and what is dynamic config (sinks are static, servicenames are dynamic)
 * Add new schema pipeline
 * Remove Sentinel exposing
 * No SSRC based pair matching, roomId is required to match the calls
 * No marker field in saved DTO
 * No serviceId serviceName resolving, only string as serviceId
 * serviceId to organizationId
 * Lambda function supports for GCP, AWS, Azure, etc.
 * Making a module handles inconsistency (like not existing media track for peer connections)
 * Task to handle inconsistency
 * Rename serviceId to organizationId, and mediaUnitId to appId

My notes:
 * No UserMediaError monitors
 * No Sentinels
 * Only through ObserverMetrics (renamed to ServiceMetrics) can put counter or anything through
 * eviction and expiration is based on hazelcast expiration. -> refresher is necessary
 
Noticable features:
 * Call matching to all reports: you do not need to join tables to know which call it belongs to
 * Matching pcs to tracks inside reports, so you instantly know from a report which track belongs to which client

Config features (can be configured dynamically):
 * obfuscations

Missing from schema:
 * timeZoneId
 * marker

 
 
## 0.8.3 (H)

### Added

### Removed
 * graphql package
 

### Refactored

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

### 0.6.x

This version added a so called `Sentinel` concept to the Observer, so Calls and Peer Connections 
can be filtered and exposed metrics through prometheus can be observed

### 0.5.x

This version removed MySQL dependency completely and used Hazelcast as distributed database.
This gives a possibility to scale observer without scaling the underlying database.
Since Hazelcast can only be embedded in java this also bounds the project to Java.

### 0.3.x

This version was the first version with the 
new schema (Reports v2, and PeerConnectionSample from the observer-js endpoint).


### 0.2.x

This version started matching calls based on SSRC of reported peer connections.

### 0.1.x

This version was build as a proof of concept to accept samples from javascript endpoint and 
using kafka streams to process them and forward generated reports to kafka.
The underlying database was MySQL.