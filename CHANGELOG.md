All notable changes to this project will be documented in this file.
We follow the [Semantic Versioning 2.0.0](http://semver.org/) format.


## 0.7.0 -

### Added
 * /sentinelfilters endpoint (GET, PUT, DELETE, POST)
 * /sentinels endpoint (GET, PUT, DELETE, POST) 
 * /servicemaps endpoint (GET, PUT, DELETE, POST)
 * Sentinels to watch calls periodically going through the pipeline and expose metrics.
 * Security improvements by adding configuration to drop unmapped samples.
 * /home endpoint for multiple OAuth2 support (Google, and AWS are added, maybe more comes)

### Deprecated
 - 

### Removed
 * All type of DTO added in `0.6.x`.
 * All Tasks 


### Fixed
 * Illmatched calls by fixed SSRCs without call names.
 * WeakLocks to handle timeouts
 * 

### Refactored
 * `repositories` package to use `ChainedTasks` newly created object performing tasks in a transactional manner.
 * All DTO objects used to store in hazelcast in-memory data grid
