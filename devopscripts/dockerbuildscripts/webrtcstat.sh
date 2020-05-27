./gradlew clean
./gradlew build :webrtcstat
cd ../webrtcstat
docker build . --no-cache -t gatekeeper:latest
