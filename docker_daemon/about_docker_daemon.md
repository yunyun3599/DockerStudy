# 도커 데몬

## docker, dockerd
컨테이너나 이미지를 다루는 명령어는 `/usr/bin/docker`에서 실행  
도커 엔진의 프로세스는 `/usr/bin/dockerd` 파일로 실행  

* docker = 클라이언트로서의 도커  
  - 도커 데몬에 보낼 API를 사용할 수 있도록 CLI를 제공   
  - 도커 클라이언트는 `/var/run/docker.sock`에 위치한 유닉스 소켓을 통해 도커 데몬의 API를 호출  
* dockerd = 서버로서의 도커  
  - 실제로 컨테이너를 생성, 실행, 이미지 관리
  - 외부에서 API를 입력받아 도커 엔진의 기능을 수행
  - 도커 프로세스가 실행되어 서버로서 입력을 받을 준비가 된 상태 = **도커 데몬**

> **도커 명령어 흐름**
>1. 사용자가 CLI를 통한 명령어 입력 (e.g. docker images)
>2. /usr/bin/docker는 /var/run/docker.sock 유닉스 소켓을 사용해 도커 데몬에게 명령어를 전달
>3. 도커 데몬은 이 명령어를 파싱하고 명령어에 해당하는 작업을 수행
>4. 수행 결과를 도커 클라이언트에게 반환하고 사용자에게 결과를 출력

## 도커 데몬 실행  
도커 데몬 실행 명령어
```sh
$ service docker start
$ service docker stop
```

서비스를 사용하지 않고 직접 도커 데몬 실행하기  
```sh
$ service docker stop   # 도커 데몬 멈추기
$ dockerd               # 도커 데몬 실행 (foreground 상태로 실행됨)
```
위처럼 도커 데몬을 실행켜두고 다른 터미널에서 도커 명령어를 입력하면 도커 사용 가능

## 도커 데몬 설정  
도커 데몬 적용 옵션 확인
```sh
$ dockerd --help
```

옵션 사용 예시
```sh
$ dockerd --insecure-registry=192.168.99.100:5000
```

옵션을 직접 주어 사용해도 되고, 설정파일의 DOCKER_OPTS에 원하는 값을 입력해서 사용할 수도 있음  
```sh
$ vi /etc/default/docker
...
DOCKER_OPTS="dockerd -H tcp://0.0.0.0:2375 --insecure-registry=192.168.100.99:5000 --tls=false"
```

## 도커 데몬 제어: -H
-H 옵션은 도커 데몬의 API를 사용할 수 있는 방법을 추가함  

- 옵션 X: 도커 클라이언트`(/usr/bin/docker)`를 위한 유닉스 소켓인 `/var/run/docker.sock`을 사용
  - 아래 두 명령어 동일
    ```sh
    $ dockerd
    $ dockerd -H unix:///var/run/docker.sock
    ```
- 옵션 O: -H에 IP 주소와 포트 번호를 입력하면 원격 API인 Docker Remote API로 도커를 제어할 수 있음  
  - 로컬에 있는 도커 데몬이 아니더라도 제어할 수 있음  
  - RESTful API 형식을 사용하므로 HTTP 요청으로 도커 제어 가능  
  - 사용 명령어 예시  
    ```sh
    $ dockerd -H tcp://0.0.0.0:2375
    ```
    이 경우 `-H unix:///var/run/docker.sock`를 지정하지 않았으므로 유닉스 소켓은 비활성화되어 도커 클라이언트를 사용할 수 없게 됨. (일반적으로 Remote API와 유닉스 소켓을 동시에 설정함)

### Remote API를 사용하여 도커 데몬에 API 보내기
```sh
# 도커 데몬 실행
$ dockerd -H tcp://192.168.99.100:2375

# curl을 통한 Http 요청
$ curl 192.168.99.100:2375/version --silent | python -m json.tool
```

Remote API의 종류는 도커 명령어의 개수만큼 있으며 API에 따라서 사용하는 방법이 도커 명령어와 조금씩 다른 부분도 있음  
따라서 HTTP로 직접 API 요청을 전송하기보다는 특정 언어로 바인딩된 라이브러리를 사용하는 것이 더 일반적임  

또한 쉘의 환경변수를 설정해 원격 도커를 제어할 수도 있음  
```sh
$ export DOCKER_HOST="tcp://192.168.99.100:2375"    # 해당 도커 데몬에 API 요청을 전달
$ docker version

# 위의 설정은 아래 명령어와 동일하게 기능
$ docker -H tcp://192.168.99.100:2375 version
```
