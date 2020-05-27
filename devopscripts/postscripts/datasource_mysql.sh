pip3 install -r datasource_init/webrtcstat_mysql/requirements.txt
docker-compose up -d mysql
sleep 30
cd datasource_init/webrtcstat_mysql
python3 run.py
cd ../../..
docker-compose stop mysql
