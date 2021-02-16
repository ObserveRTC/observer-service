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

### Configuration

This microservice is built by using the [micronaut](https://micronaut.io) framework. 
The configuration fil
By deploying it the configuration file allow to configure the micronaut itself. 
The following features for micronaut is added in this microservice:

 * [Kubernetes](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/index.html)
   * [Kubernetes documentation](https://micronaut-projects.github.io/micronaut-kubernetes/latest/guide/index.html)
 * [HTTP Client](https://docs.micronaut.io/latest/guide/index.html#httpClient)
 * [OpenAPI Support](https://micronaut-projects.github.io/micronaut-openapi/latest/guide/index.html)
 * [RxJava 3](https://micronaut-projects.github.io/micronaut-rxjava3/snapshot/guide/index.html)
 * [JMX endpoints](https://micronaut-projects.github.io/micronaut-jmx/latest/guide/index.html)
 * [Management Endpoints](https://docs.micronaut.io/latest/guide/index.html#management)
 * [Security-jwt feature](https://micronaut-projects.github.io/micronaut-security/latest/guide/index.html)


### Configuration



 


