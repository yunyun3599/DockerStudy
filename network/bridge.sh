#!/usr/bin/env sh

docker network create --driver=bridge bridgenetwork

docker run -d --network=bridgenetwork --net-alias=nginx nginx
docker run -d --network=bridgenetwork --net-alias=grafana grafana/grafana

