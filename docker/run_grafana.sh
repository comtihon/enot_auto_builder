#!/usr/bin/env bash
echo 'Starting Grafana...'
/run.sh "$@" &
AddDataSource() {
  curl 'http://admin:admin@localhost:3000/api/datasources' \
    -X POST \
    -H 'Content-Type: application/json;charset=UTF-8' \
    --data-binary \
    '{"name":"influx","type":"influxdb","url":"http://influxdb:8086","access":"proxy","isDefault":true, "database":"collectd","user":"metrics","password":"password"}'
}
until AddDataSource; do
  echo 'Configuring Grafana...'
  sleep 1
done
echo 'Done!'
wait