# WebRTC-Observer

A microservice to monitor WebRTC Systems.

## Quick Install & Run

Run

      ./install.sh

This build the project and the docker containers.

If you are using BigQuery, before you 
run the service check the following 
configurations in docker-compose.yml file webrtc_observer 
service:

      - REPORTSINK_BIGQUERY_PROJECT_NAME=observertc
      - REPORTSINK_BIGQUERY_DATASET_NAME=WebRTC
      - REPORTSINK_BIGQUERY_CREATE_DATASET_IF_NOT_EXISTS=True

Setup those accordingly. 
Check volumes section at the service
       
       - /path/to/your/credentials:/bg_credential.json

and setup the path accordingly.
Then:

    docker-compose up