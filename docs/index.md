Observer Documentation
===

# Introduction

WebRTC-Observer is a microservice written in Java to collect and analyze 
measurements originated by WebRTC Applications. It is designed to be a 
monitoring solution scalable from a single, local development up until 
a large kubernetes cluster.  


# Architecture

![Architecture](architecture.png)

In our architectural model, the observer takes place as a 
processing element, capable of accepting samples from the 
collecting components, and can forward reports to a Data Sink.

You can read a detailed description about the architecture 
in our [main](http://observertc.org) page.

The observer main responsibilities are the following:
 * Accept samples
 * Provide metrics
 * Forward reports

## Accept Samples

THe service can listen for samples on various 
inputs (e.g. websocket server), and samples are accepted if 
the invoked input validate the provided samples.



### Peer Connection Sample

 * Websocket


## Frameworks

### Micronaut 

### Hazelcast

# Services

## Mappings

# Metrics

## Sentinels

## Collection Filtering

## Call Filters

## Peer Connection Filters


# Security

## Authentication

## Obfuscation



# Versions

The version number (more or less) follows [semantic versioning](https://semver.org/).
The major versions are named, and tagged.

Major versions:
 * [hydrogen](hydrogen.md) The first viable service of the product

# Deployment

## Kubernetes

## Docker


