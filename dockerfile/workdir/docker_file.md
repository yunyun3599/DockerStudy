## Dockerfile
```Dockerfile
FROM ubuntu:20.04
MAINTAINER yoonjae
LABEL "purpose"="practice"
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update
RUN apt-get install apache2 -y
ADD test.html /var/www/html
WORKDIR /var/www/html
RUN ["/bin/bash", "-c", "echo hello >> test2.html"]
EXPOSE 80
CMD apachectl -DFOREGROUND
```
1. FROM: 베이스 이미지
2. MAINTAINER: 관리자 (메타 정보)
3. LABEL: 관련 정보 메타 정보
4. ARG: 환경변수
5. RUN: 명령어 수행
6. ADD: 이미지로 파일 복사
7. WORKDIR: 작업 디렉터리 (cd 역할)
8. RUN: 명령어 수행 시 배열 형태로 사용 가능 `["실행 가능한 파일", "명령줄 인자 1", "명령줄 인자 2", ...]`  
9. EXPOSE: 사용하는 포트 번호 (호스트와 바인딩 하려면 -P 옵션 필요 `docker run -d -P image:tag`)
10. CMD: 도커 컨테이너 실행 시 기본적으로 수행할 명령어

## 빌드
**빌드**
```sh
$ docker build -f Dockerfile2 -t dockerfile_test:1 .
```
**실행**
```sh
$ docker run -d -P --name test dockerfile_test:1
```

빌드 컨텍스트는 Dockerfile이 위치한 디렉터리이므로 루트 디렉터리 같이 너무 상단 디렉터리에서 하면 필요 없는 파일까지 빌드 컨텍스트에 들어가서 메모리에 부담이 됨  
따라서 사용할 디렉터리를 따로 마련한 후에 진행하는 것이 조음  

빌드는 기본적으로 캐시를 이용해서 수행됨  
캐시 사용을 원치 않으면 `docker build --no-cache -t dockerfile_test:1 .` 로 옵션을 주어 진행  
캐시 사용을 할 이미지를 직접 지정하려면 `docker build --cache-from test:1 -f Dockerfile2 -t dockerfile_test:2 .` 로 옵션을 주어 진행  

## .dockerignore
```.dockerignore
test2.html
*.html
*/*.html
test.htm?
!test*.html
```
목록에 있는 파일을 빌드 컨텍스트에서 제외  
- *.html: 임의의 이름의 html 파일 제외
- */*.html: 한 뎁스 안 디렉터리에 있는 html 파일 제외
- test.htm?: test.htma, test.htmb, test.htmc 등 test.htm을 접두어로 갖고 뒤에 한 글자가 더 오는 파일 제외
- !test*.html: 앞에 !가 들어가면 뒤의 조건의 이름을 갖는 파일은 빌드 컨텍스트에 다시 추가

## 멀티 스테이지를 이용한 Docker 빌드  
다음 도커 파일을 빌드
```Dockerfile
FROM python
ADD main.py /root
WORKDIR /root
CMD ["python", "./main.py"]
```
```sh
$ docker build -f DockerfileMultiStage -t dockerfile_test:python .
```
이미지 크기 확인
```
dockerfile_test     python       3af189021134   38 seconds ago   871MB
```
실제 파일 크기는 작지만 소스코드 빌드에 사용된 각종 패키지 및 라이브러리가 불필요하게 이미지 크기를 차지함  


**멀티스테이지** 빌드 방법을 사용해 이미지 크기 줄일 수 있음  
하나의 Dockerfile 안에 여러 개의 FROM 이미지를 정의해 빌드 완료 시 최종 생성되는 이미지 크기를 줄이는 역할  
```Dockerfile
FROM python as builder
ADD main.py /root
WORKDIR /root

FROM alpine:latest
WORKDIR /root
COPY --from=builder /root/main.py .
CMD ["python", "./main.py"]
```
```sh
$ docker build -f DockerfileMultiStage2 -t dockerfile_test:python2 .
```
```
dockerfile_test                                         python2      3217a07dc15c   5 seconds ago    7.46MB
dockerfile_test                                         python       3af189021134   4 minutes ago    871MB
```

## 추가 명령어
### ENV
환경변수 세팅  
실행된 컨테이너 내에서도 사용 가능
```Dockerfile
FROM ubuntu:20.04
ENV test /home
WORKDIR $test
```  

- `${env_name:-value}`: env_name이라는 환경변수의 값이 설정되지 않았으면 이 환경변수의 값을 value로 사용
- `${env_name:+value}`: env_name이라는 환경변수의 값이 설정되어있으면 value를 값으로 사용하고 값이 설정되지 않았다면 빈 문자열을 사용


### VOLUME  
호스트와 공유할 컨테이너 내부 디렉터리 설정  
여러 개 사용 가능
```Dockerfile
FROM ubuntu:20.04
RUN mkdir /home/volume
VOLUME /home/volume
```

### Onbuild
빌드된 이미지를 기반으로 하는 다른 이미지가 Dockerfile로 생성될 때 실행할 명령어를 추가  
```Dockerfile
FROM ubuntu:20.04
ONBUILD RUN echo "onbuild!" >> /onbuild_file
```
위의 도커파일을 빌드할 때 `ONBUILD`라인은 실행되지 않다가 다른 Dockerfile에서 앞의 도커파일로 인해 생성된 이미지를 베이스 이미지로 실행할 때 수행됨  

### STOPSIGNAL  
컨테이너가 정지될 때 사용할 시스템 콜의 종류를 지정  
기본값은 SIGTERM 
```Dockerfile
FROM ubuntu:20.04
STOPSIGNAL SIGKILL
```
참고로 docker run 명령어에서 `--stop-signal` 옵션으로 컨테이너에 개별젹으로 선택 가능  

### HEALTHCHECK
이미지로부터 생성된 컨테이너엔서 동작하는 애플리케이션의 상태를 체크하도록 설정  
애플리케이션의 프로세스가 종료되진 않았으나 애플리케이션이 동작하지 않는 경우를 방지하기 위해 사용  
```Dockerfile
FROM nginx
RUN apt-get update -y ** apt-get install curl -y
HEALTHCHECK --interval=1m --timeout=3s --retries=3 CMD curl -f http://localhost || exit 1
```

### SHELL
사용하려는 shell 종류 명시할 때 사용  
```Dockerfile
FROM ubuntu:20.04
RUN echo hello, node!
SHELL ["/usr/local/bin/node"]
RUN -v
```

### ADD와 COPY의 차이점  
COPY는 로컬의 파일만 이미지에 추가 가능  
ADD는 외부 URL 및 tar 파일에서도 파일을 추가 가능  

```Dockerfile
ADD https://raw.githubusercontent.com/alicek106/mydockerrepo/master/test.html /home
```

tar 파일 추가 시에는 tar파일을 자동으로 해제해서 추가함  
```Dockerfile
ADD test.tar /home
```

ADD는 어떤 파일이 추가될 지 정확히 알기 어려우므로 사용하는 것 권장하지 않음  


### Entrypoint & CMD
```sh
$ docker run -ti --name no_entrypoint ubuntu:20.04 /bin/bash    # entrypoint 없음, cmd: /bin/bash
```
```sh
$ docker run -ti --entrypoint="echo" --name yes_entrypoint ubuntu:20.04 /bin/bash    # entrypoint echo, entrypoint의 파라미터값: /bin/bash
# 결과적으로 /bin/bash라는 문자열이 터미널에 출력됨  
```

일반적으로 스크립트를 주어 entrypoint를 이용해 실행함
```sh
$ docker run -ti --entrypoint="./test.sh" --name entrypoint_sh ubuntu:20.04 /bin/bash
```

entrypoint.sh 사용 도커파일
```Dockerfile
FROM ubuntu:20.04
RUN apt-get update
RUN apt-get install apache2 -y
ADD entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/bin/bash", "/entrypoint.sh"]
```

entrypoint.sh 파일
```sh
echo $1 $2
apachectl -DFORGEGROUND
```

실행 명령어
```sh
$ docker build -t entrypoint_image:0.0 .
$ docker run -d --name entrypoint_apache_server entrypoint_image:0.0 first second   # entrypoint.sh에서 쓰이는 인자 전달
```

### JSON 배열형태  
JSON 배열 형태로 입력하지 않으면 CMD와 ENTRYPOINT에 명시된 명령어 앞에 `/bin/sh -c` 가 추가됨  
```sh
CMD echo test   # /bin/sh -c echo test
ENTRYPOINT /entrypoint.sh   # /bin/sh -c entrypoint.sh
```

```sh
CMD ["echo", "test"]          # echo test
ENTRYPONT ["/bin/bash", "/entrypoint.sh"]   # /bin/bash /entrypoint.sh
```

### 도커 파일 작성 유의점
좋은 습관
- 하나의 명령어를 \로 나누어 가독성을 높일 수 있도록 작성
  ```Dockerfile
    FROM ubuntu:20.04
    RUN apt-get install package-1 \
                        package-2 \
                        package-3
  ```
- .dockerignore파일을 작성
- 빌드캐시 이용

이미지에 명령어에 따라 레이어가 추가되므로 어떤 파일을 추가했다가 밑에서 삭제해도 추가했던 레이어가 존재하고 그 후에 삭제된 레이어가 또 생성된 것이므로 이미지 용량이 커짐  
이를 방지하려면 한 명령어에 생성과 삭제를 모두 넣으면 됨  
```Dockerfile
FROM ubuntu:20.04
RUN mkdir /test && \
    fallocate -l 100m /test/dummy && \
    rm /test/dummy
```


+ 다른 사람이 빌드한 이미지에 불필요한 레이어가 있다면 해당 이미지를 실행해 컨테이너를 만든 후 작업을 하고 컨테이너를 export, import 하는 방법을 사용할 수 있음  
-> 레이어가 1개로 줄어들게 됨. 다만 각종 이미지 설정은 잃게 되므로 주의!