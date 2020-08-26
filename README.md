# WebRTC-Observer

Microservice to monitor WebRTC Applications.

## Quick Deploy & Run

Use docker to build and run the infrastructure

      docker-compose up kafka mysql webtc_observer

If you want to use BigQuery Reporting service, you should obtain 
a credential json file from GCP to your BigQuery copy it to the 
directory you run the `docker-compose` as google_api_creedentials.json.
Then 

    docker-compose up kafka mysql webtc_observer webtc_bigquery_reporter
    
You can configure the [Observer](https://hub.docker.com/repository/docker/observertc/webrtc-observer), 
and the [BigQuery Reporter](https://hub.docker.com/repository/docker/observertc/webrtc-bigquery-reporter) 
via environment variables. 


## Contribute 


## License

