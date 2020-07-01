# GateKeeper

## Install Guideline

### Install and Compile the service

    ./install.sh --service webrtcstat --datasource mysql

Keep in mind, that the project should be build with java 12, so If the gradle complain about building, try to point to a JAVA 12 Home directory:

    JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-12.0.2.jdk/Contents/Home/ ./gradlew build
    
### Add BigQuery

1. Create your own Dataset under BigQuery and add the following tables:
  
    
    CREATE TABLE WebRTC.InitiatedCalls 
    (
    observerUUID STRING, 
    callUUID STRING,
    initiated TIMESTAMP,
    );
    
    CREATE TABLE WebRTC.FinishedCalls 
    (
    observerUUID STRING, 
    callUUID STRING,
    finished TIMESTAMP,
    );
    
    CREATE TABLE WebRTC.JoinedPeerConnections 
    (
    observerUUID STRING, 
    callUUID STRING,
    peerConnectionUUID STRING,
    joined TIMESTAMP
    );
    
    CREATE TABLE WebRTC.DetachedPeerConnections 
    (
    observerUUID STRING, 
    callUUID STRING,
    peerConnectionUUID STRING,
    detached TIMESTAMP
    );
    
    
    CREATE TABLE WebRTC.StreamSamples
    (
        observerUUID STRING,
        peerConnectionUUID STRING,
        SSRC INT64,
        RTT STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        packetsSent STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        packetsReceived STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        bytesSent STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        bytesReceived STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        packetsLost STRUCT<
            minimum INT64,
            maximum INT64,
            sum INT64,
            presented INT64,
            empty INT64
        >,
        firstSample TIMESTAMP,
        lastSample TIMESTAMP
    );


2. Generate your Credentials in json format from your GCP. (Following steps: https://cloud.google.com/docs/authentication/production)

3. Change the value of GOOGLE_APPLICATION_CREDENTIALS at docker-compose.yml to the path you have the file

4. Run docker in docker
        
        docker-compose up



#### Development notes, will be deleted from here
If the jooq not generates, change the inputSchema to public, run and then change it back
