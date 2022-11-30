# 도커 볼륨

## 호스트 볼룸
### 명령어
```sh
$ docker run -v [마운트할 로컬 경로]:[/마운트할 컨테이너 경로] [이미지]
```
### 사용 예
```sh
$ docker run -ti --name test -v ~/study/docker-study/volume:/data ubuntu bash
$ /data/print.sh
```

## 볼륨 컨테이너
### 명령어
```sh
$ docker run --volumes-from [volume 컨테이너] [이미지]
```
### 사용 예
```sh
$ docker run -d -ti --name test --rm -v ~/study/docker-study/volume:/data ubuntu bash

$ docker run -ti --volumes-from test ubuntu bash
```

## 도커 볼륨
### 명령어
```sh
$ docker volume create --name [볼륨 이름]
$ docker run -v [볼륨이름]:[마운트 경로] [이미지]
```
### 사용 예
```sh
$ docker volume create --name db

$ docker run \
 -d \
 --name mysql \
 -e MYSQL_DATABASE=volumepractice \
 -e MYSQL_ROOT_PASSWORD=volumepractice \
 -v db:/var/lib/mysql \
 -p 3306:3306 \
 mysql
```

## 읽기 전용 볼륨 연결
### 명령어
```sh
$ docker run -v [볼륨이름]:[마운트경로]:ro [이미지명]
```
### 사용 예
```sh
docker run -d -ti --name test --rm -v ~/study/docker-study/volume:/data:ro ubuntu bash
```