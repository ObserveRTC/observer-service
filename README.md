# GateKeeper

## Quick Install

    ./install.sh --service webrtcstat --datasource mysql --subscriber elasticsearch

Keep in mind, that the project should be build with java 12, so If the gradle complain about building, try to point to a JAVA 12 Home directory:

    JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-12.0.2.jdk/Contents/Home/ ./gradlew build
    
#### Development notes, will be deleted from here
If the jooq not generates, change the inputSchema to public, run and then change it back
