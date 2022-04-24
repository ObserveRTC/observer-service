# Contribution Guideline

## Finding Issues to Work on

If you are interested in contributing to ObserveRTC 
and are looking for issues to work on, take a look at the issues  
tagged with [help wanted](https://github.com/ObserveRTC/observer/labels/help%20wanted).

## JDK Setup

The application currently requires JDK 17

## IDE Setup

The application can be imported into IntelliJ IDEA by opening the `build.gradle` file.

## Docker Setup

Docker image is published for each released version to [Dockerhub](https://hub.docker.com/repository/docker/observertc/observer) 

## Running Tests

To run the tests use `gradle test`.

## Building Documentation

The documentation sources are located at `docs/`.

## Working on the code base

If you are working with the IntelliJ IDEA development 
environment, you can import the project using the Intellij 
Gradle Tooling ( "File / Import Project" and select the 
"settings.gradle" file).

To get a local development version of the service working, 
first run the `build` task.

```
./gradlew build
```
The service itself is to receive Peer Connection Samples 
from WebRTC calls, analyze them, making and forwarding WebRTC-Reports. 
 For local development see [full stack examples repository](https://github.com/ObserveRTC/full-stack-examples), then 
run your observer locally and check the results.
For development check out the 
[development manual](https://observertc.github.io/observer/). 

## Creating a pull request

Once you are ready with your changes:

- Commit your changes in your local branch
- Push your changes to your remote branch on GitHub
- Send us a [pull request](https://help.github.com/articles/creating-a-pull-request)

## Checkstyle

We want to keep the code clean,
following good practices about organization, javadoc and 
style as much as possible.
