All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.

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
