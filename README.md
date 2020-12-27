WebRTC-Observer
===

WebRTC-Observer is a microservice collect and analyze 
samples provided by a WebRTC application and make 
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

### Micronaut

This microservice is built by using the [micronaut](https://micronaut.io) framework. 
By deploying it the configuration file allow to configure the micronaut itself. 
The following features for micronaut is added in this microservice:

 * [Kubernetes](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/index.html)
   * [Prometheus annotation](https://github.com/dekorateio/dekorate#prometheus-annotations)
   * [Kubernetes documentation](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/index.html)
   * [Kubernetes annotation](https://github.com/dekorateio/dekorate#kubernetes)
 * [HTTP Client](https://docs.micronaut.io/latest/guide/index.html#httpClient)
 * [OpenAPI Support](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)
 * [RxJava 3](https://micronaut-projects.github.io/micronaut-rxjava3/snapshot/guide/index.html)
 * [Kafka](https://micronaut-projects.github.io/micronaut-kafka/latest/guide/index.html)
 * [JMX endpoints](https://micronaut-projects.github.io/micronaut-jmx/latest/guide/index.html)
 * [Management Endpoints](https://docs.micronaut.io/latest/guide/index.html#management)
 * [Security-jwt feature](https://micronaut-projects.github.io/micronaut-security/latest/guide/index.html)


### Compatibility  

The deployed version has compatibility with other components.

|                                                        |   0.4     |
|--------------------------------------------------------|-----------|
| [webextrapp](https://github.com/ObserveRTC/webextrapp) | v0.1.2<=  | 

### Environment variable

You can define your own environment variable in the configuration by 
following [this](https://docs.micronaut.io/latest/guide/index.html#propertySource).
The following environment variables are found in the default configuration.

 * `MICRONAUT_CONFIG_FILES` (*Default: NULL*): Defines the external config yaml micronaut microservice has to consider in its bootstrap.
 * `HAZELCAST_CONFIG_FILE` (*Default: NULL*): Defines the external config yaml micronaut microservice has to consider in its bootstrap.
 * `INITIAL_WAITING_TIME_IN_S` (*Default: 0*): Defines an initial waiting time for the application before it actually starts. Useful if the infrastructure takes time to setup.
 * `APPLICATION_SERVER_PORT` (*Default: 7080*): Defines the port the application listens http requests.
 * `APPLICATION_MANAGENEMT_PORT` (*Default: 7081*): Defines the port the application listens management requests. 
 * `METRICS_ENABLED` (*Default: False*): Defines if the /health endpoint is exposed or not.
 * `KAFKA_HOSTS` (*Default: localhost:9092*): Defines the hosts for kafka used by the Observer.


 


