#!/usr/bin/env sh

docker volume create --name db

docker volume ls

docker run \
 -d \
 --name mysql \
 -e MYSQL_DATABASE=volumepractice \
 -e MYSQL_ROOT_PASSWORD=volumepractice \
 -v db:/var/lib/mysql \
 -p 3306:3306 \
 mysql
