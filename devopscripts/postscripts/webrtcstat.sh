cd ..
./gradlew clean
./gradlew build
cd webrtcstat
docker build . --no-cache -t gatekeeper:latest
cd ..
cd devopscripts
