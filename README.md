# WebRTC-Observer

Microservice to monitor WebRTC Applications.

## Quick Deploy & Run

Use docker to build and run the infrastructure

      docker-compose up kafka mysql webtc_observer

If you want to use BigQuery Reporting service, you should obtain 
a credential json file from GCP to your BigQuery copy it to the 
directory you run the `docker-compose` as google_api_credentials.json.
Then 

    docker-compose up kafka mysql webtc_observer webtc_reporter
    
You can configure the [Observer](https://hub.docker.com/repository/docker/observertc/webrtc-observer), 
and the [Reporter](https://hub.docker.com/repository/docker/observertc/webrtc-reporter) 
via environment variables. 


## Contribute 


## License

