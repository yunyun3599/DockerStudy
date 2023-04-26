# 도커 컴포즈  
## 도커 컴포즈 사용 이유  
여러 컨테이너로 구성된 애플리케이션을 구축하는 경우 각 컨테이너 별로 run 명령어를 여러 번 사용하는 것은 번거로움  
매번 명령어에 옵션을 설정해 CLI로 컨테이너를 생성하기보다는 여러 개의 컨테이너를 하나의 서비스로 정의해 컨테이너 묶음으로 관리하는 것이 더 편리  


**도커 컴포즈**는 컨테이너를 이용한 서비스의 개발과 CI를 위해 여러 개의 컨테이너를 하나의 프로젝트로서 다룰 수 있는 작업 환경을 제공  

도커 컴포즈는 여러 개의 컨테이너의 옵션과 환경을 정의한 파일을 읽어 컨테이너를 순차적으로 생성하는 방식으로 동작  
run 명령어의 옵션을 그대로 사용할 수 있으며 각 컨테이너의 의존성, 네트워크, 볼륨 등을 함께 정의할 수 있음  
서비스의 컨테이너 수를 유동적으로 조절할 수 있음  
컨테이너의 서비스 디스커버리도 자동으로 이뤄짐  

> 컨테이너 수가 많아지고 정의해야 할 옵션이 많아진다면 도커 컴포즈를 사용하는 것이 좋음  

## 도커 컴포즈 설치  
리눅스
```sh
$ curl -L https://github.com/docker/compose/releases/download/v2.17.3/docker-compose-`uname -s`-`uname -m` > /usr/local/bin/docker-compose
$ chmod +x /usr/local/bin/docker-compose
```
 윈도우, 맥에는 Docker Desktop 설치 시 함께 설치됨  

 도커 컴포즈 버전 확인  
 ```sh
$ docker-compose -v
 ```

## 도커 컴포즈 사용  
### 도커 컴포즈 기본 사용법  
컨테이너 설정이 정의된 YAML 파일을 읽어 도커 엔진을 통해 컨테이너 생성  

### docker-compose.yml 작성 및 활용  
도커 컴포즈 사용법을 알아보기 위해 다음과 같은 run 명령어를 docker-compose.yml 파일로 변환해 컨테이너를 생성하고 실행하기  
```sh
$ docker run -d --name mysql \
  alicek106/composetest:mysql \
  mysqld

$ docker run -d -p 80:80 \
  --link mysql:db --name web \
  alicek106/composetest:web \
  apachectl -DFOREGROUND
```

docker-compose.yml 파일 작성  
```yaml
version: '3.0'    # yaml 파밀 포맷의 버전을 의미
services:         # 생성될 컨테이너들을 묶어놓은 단위
  web:            # 생성될 서비스의 이름. 아래에 컨테이너가 생성될 때 필요한 옵션을 지정할 수 있음
    image: alicek106/composetest:web
    ports:
      - "80:80"
    links:
      - mysql:db
    command: apachectl -DFOREGROUND
  mysql:
    image: alicek106/composetest:mysql
    command: mysqld
```

별다른 설정이 없으면 도커 컴포즈는 현재 디렉터리의 docker-compose.yml 파일을 읽어 로컬의 도커 엔진에게 컨테이너 생성을 요청함  

백그라운드로 도커 컴포즈 파일을 실행하면 -d 옵션을 주어 아래와 같이 명령어를 수행시키면 됨  
```sh
$ docker-compose up -d
```

## 도커 컴포즈의 프로젝트, 서비스 컨테이너
도커 컴포즈는 컨테이너를 프로젝트 및 서비스 단위로 구분하므로 컨테이너의 이름은 일반적으로 다음과 같은 형식으로 정해짐  
> `[프로젝트이름]_[서비스이름]_[서비스내에서 컨테이너의 번호]`
`docker compose up` 실행 시에 프로젝트 이름을 별도로 입력하지 않는다면 도커 컴포즈는 기본적으로 docker-compose.yml 파일이 위치한 디렉터리의 이름을 프로젝트 이름으로 사용  

하나의 프로젝트는 여러 개의 서비스로 구성되고 각 서비스는 여러 개의 컨테이너로 구성됨  
스웜 모드에서의 서비스와 마찬가지로 하나의 서비스에는 여러 개의 컨테이너가 존재할 수 있으므로 차례대로 증가하는 컨테이너의 번호를 붙여 서비스 내의 컨테이너를 구분함  

컨테이너 개수를 여러개로 늘리는 명령어는 `docker-compose scale` 명령어  
```sh
$ docker-compose scale mysql=2
```

> docker-compose up 명령어의 끝에 서비스의 이름을 입력해 docker-compose.yml 파일에 명시된 특정 서비스의 컨테이너만 생성 가능
```sh
$ docker-compose up -d mysql    # mysql 서비스의 컨테이너만 생성
```

`docker-compose run` 명령어로 컨테이너를 생성할 수도 있음 (이 때는 Interactive shell 사용도 가능)   
```sh
$ docker-compose run web /bin/bash
```

생성된 프로젝트는 `docker-compose down` 명령어로 삭제 가능  
프로젝트를 삭제하면 서비스 컨테이너 또한 전부 정지된 뒤 삭제됨  

도커 컴포즈는 기본적으로 현재 디렉터리의 이름으로 된 프로젝트를 제어함  
`/home/ubuntu` 라는 디렉터리에 `docker-compose.yml` 파일이 있고 `docker-compose down` 명령어를 사용하면 ubuntu 라는 이름을 가진 프로젝트를 삭제함  
그러나 `docker-compose`의 `-p` 옵션을 사용하면 프로젝트의 이름을 사용해 제어할 프로젝트의 이름을 명시할 수 있음  
```sh
$ docker-compose -p myproject up -d
$ docker-compose -p myproject ps
$ docker-compose -p myproject down
```
