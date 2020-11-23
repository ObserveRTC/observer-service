# What is WebRTC-Observer?

WebRTC-Observer is a microservice collect and analyze 
measurements provided by WebRTC applications and make 
WebRTC Reports.  

## What is a WebRTC Report? 

WebRTC Reports is a collection of information provided by the Observer.
Each Report has the following properties:
 - service Id: The service the report belongs to 
 - mediaUnit Id: The media unit the report belongs to
 - timestamp: The epoch timestamp in ms when the report was created
 - type: the actual type of the report
 - payload: the payload regarding to the type
 
The provided report has a schema available here.


## Deploy WebRTC-Observer

If your aim is to use it in your environment 
we highly recommend you our other [repository](https://github.com/ObserveRTC/WebRTC-Deployments) 
dedicated for this issue.


### Compatibility  

The deployed version has compatibility with other components.

|                                                        |   0.4     |
|--------------------------------------------------------|-----------|
| [webextrapp](https://github.com/ObserveRTC/webextrapp) | v0.1.2<=  | 




## Environment variables

### General Settings

 - `INITIAL_WAITING_TIME_IN_S` (*Default: 0*): Defines an initial waiting time for the application before it actually starts. Useful if the infrastructure takes time to setup.
 - `APPLICATION_SERVER_PORT` (*Default: 8088*): Defines the port the application listens http requests. 
 - `METRICS_ENABLED` (*Default: False*): Defines if the /health endpoint is exposed or not. 
 
### Database Setup

 - `MICRONAUT_CONFIG_FILES` (*Default: NULL*): Defines the external config yaml micronaut microservice has to consider in its bootstrap.
 - `CONNECTION_POOL_MIN_IDLE_IN_MS` (*Default: 100*): Define the minimum idle time for the connection pool.
 - `CONNECTION_POOL_MAX_IDLE_IN_MS` (*Default: 1000*): Define the maximum idle time for the connection pool.
 - `CONNECTION_POOL_MAX_POOL_SIZE` (*Default: 100*): Defines the maximum size of the connection pool at an Observer instant.
 - `CONNECTION_POOL_NAME` (*Default: webrtc_observer*): Defines the name of the connection pool.
 - `DATABASE_USERNAME` (*Default: root*): The username for the database the Observer is using.
 - `DATABASE_PASSWORD`(*Default: password*): The password for the database.
 - `JDBC_URL` (*Default: jdbc:mysql://localhost:3306/WebRTCObserver*): org.observertc.webrtc.observer.A URL fo the database the connection pool is connecting to using the provided username and password.

### Kafka Setup

 - `KAFKA_HOSTS` (*Default: localhost:9092*): Defines the hosts for kafka used by the Observer.
 - `KAFKA_TOPICS_OBSERVED_SAMPLES` (*Default: observedPeerConnectionSamples*) Defines the name of the topic of the incoming samples forwarded from clients.
 - `KAFKA_TOPICS_OBSERVERTC_REPORTS` (*Default: observertcReports*): Defines the name of the topic the produced Reports are forwarded originated from the Observer.
 - `KAFKA_TOPICS_OBSERVERTC_REPORTDRAFTS`  (*Default: observertcReportDrafts*): Define the name of the topic the Observer used for processing internal report drafts. 

### Sample Evaluators

 - `MEDIA_STREAM_MAX_IDLE_TIME_IN_S` (*Default: 30*): Defines the maximum allowed idle time for a Peer Connection to be idle before it is declared to be detached from a call.
 - `REPORT_OUTBOUND_RTPS` (*Default: True*): Determines if Outbound RTP Reports are made or not.
 - `REPORT_INBOUND_RTPS` (*Default: True*): Determines if Inbound RTP Reports are made or not.
 - `REPORT_REMOTE_INTBOUND_RTPS` (*Default: True*): Determines if Remote Inbound RTP Reports are made or not.
 - `REPORT_TRACKS` (*Default: True*): Determines if Track Reports are made or not.
 - `REPORT_MEDIA_SOURCES` (*Default: True*): Determines if Media Source Reports are made or not.
 - `REPORT_ICE_CANDIDATE_PAIRS` (*Default: True*): Determines if ICE Candidate Pair Reports are made or not.
 - `REPORT_LOCAL_ICE_CANDIDATES` (*Default: True*): Determines if Local ICE Candidate Reports are made or not.
 - `REPORT_REMOE_ICE_CANDIDATES` (*Default: True*): Determines if Remote ICE Candidate Reports are made or not.
 - `PEERCONNECTION_RETETNTION_TIME_IN_DAYS` (*Default: 60*): Defines the retention time for a Peer Connection to be stored in the Observer local database.
 - `CALL_CLEANER_MAX_ALLOWED_UPDATED_GAP_IN_S` (*Default: 3600*): Defines the maximum number of gap between the UTC wall clock from the Observer, and the UTC time of the last updated and joined peer connection. 
In case the service restarted the Observer is looking for a joined peer connection in its local database recently to compare peer connections, that are not updated. If it founds a joined peer connection in its local database, then it uses that timestamp, but only if the gap between the wall clock and the found timestamp 
is less than the maximum configured one.


## Develop

If your aim is to develop the Observer, or any other components 
then you need to run the Observer locally.

### Prerequisites

TODO: TBD

      docker-compose up -d

