# 도커 컴포즈

# 도커 컴포즈 활용  
## YAML 파일 작성  
도커 컴포즈를 사용하려면 컨테이너 설정을 저장해놓은 YAML 파일이 필요함  
기존에 도커 컨테이너를 실행시키기 위해 사용했던 `run` 명령어와 각종 옵션들을 YAML로 변환하여 작성하는 것이 도커 컴포즈 사용법의 대부분임

### YAML 파일 4가지 항목 정의
1. 버전 정의
2. 서비스 정의
    - 가장 많이 사용됨
3. 볼륨 정의
    - 서비스로 생성된 컨테이너에 선택적으로 적용
4. 네트워크 정의
    - 서비스로 생성된 컨테이너에 선택적으로 적용

YAML 작성 시 각 항목의 하위 항목을 정의하려면 2개의 공백으로 들여쓰기 해서 상위 항목과 구분함  
```yaml
version: '3.0'

services:
  service_1:
    image: docker/image
    container_name: test_container
...
```

도커 컴포즈는 기본적으로 현재 디렉터리나 상위 디렉터리에서 `docker-compose.yml`이라는 이름의 YAML 파일을 찾아서 컨테이너를 생성함  
만약 다른 위치에 있는 도커 컴포즈 파일을 사용하거나 파일명이 `docker-compose.yml`이 아닌 파일을 사용하고 싶다면 -f 옵션을 사용하여 컨테이너를 생성할 수 있음  
```sh
$ docker-compose -f /home/file/dir/test-docker-compose.yml up -d
```

특정 YAML 파일에서 생성된 여러 프로젝트를 제어하려면 해당 YAML 파일이 위치한 경로에서 명령어를 수행시키거나, `-f` 옵션으로 경로를 지정해야함.  
따라서 먼저 `-f` 옵션을 통해 YAML 파일을 지정해 파일을 읽은 후 `-p` 옵션으로 프로젝트의 이름을 따로 명시하는 식으로 `-f` 옵션과 `-p` 옵션을 함께 사용할 수 있음  

### 버전 정의  
YAML 파일 포맷 버전 종류: 1, 2, 2.1, 3  
도커 컴포즈 버전 1.10에서 사용 가능한 버전 3을 기준으로 정리  
(버전 3은 도커 스웜 모드와 호환되는 버전)  

버전 정의 방식  
```yaml
version: '3.0'
```

### 서비스 정의  
서비스는 도커 컴포즈로 생성할 컨테이너 옵션을 정의  
각 서비스는 컨테이너로 구현되며 하나의 프로젝트로서 도커 컴포즈에 의해 관리됨  
서비스의 이름은 services의 하위 항목으로 정의하고 각 컨테이너별 옵션은 서비스의 이름 하위 항목에 정의  

서비스 정의 방식
```yaml
services:
  my_container_1:
    image: ...
  my_container_2:
    image: ...
```

서비스 항목의 주요 컨테이너 옵션  
1. image
    - 서비스 컨테이너를 생성할 때 쓰일 이미지의 이름을 설정  
    - 이미지가 존재하지 않으면 저장소에서 내려받음  
    ```yaml
    services:
      my_container_1:
        image: alicek106/composetest:mysql
    ```
2. links
    - docker run 명령어의 `--link`와 동일 
    - 다른 서비스에 서비스명만으로 접근할 수 있도록 설정함  
    - `[SERVICES:ALIAS]` 형식을 사용하면 서비스에 별칭으로도 접근 가능  
    ```yaml
    services:
      web:
        links:
          - db
          - db:database
          - redis
    ```
3. environment
    - docker run 명령어의 `--env`, `-e` 옵션과 동일 
    - 컨테이너 내부에서 사용할 환견변수를 지정하며, Dictionary나 배열 형태로 사용 가능
    ```yaml
    services:
      web:
        environment:
          - MYSQL_ROOT__PASSWORD=mypassword
          - MYSQL_DATABASE_NAME=mydb
    ```
    ```yaml
    services:
      web:
        environment:
          MYSQL_ROOT__PASSWORD: mypassword
          MYSQL_DATABASE_NAME: mydb
    ```
3. command
    - 컨테이너가 실행될 때 수행할 명령어를 설정 
    ```yaml
    services:
      web:
        image: alicek106/composetest:web
        command: apachectl -DFOREGROUND
    ```
    - 또는 배열 형태로도 사용 가능
    ```yaml
    services:
      web:
        image: alicek106/composetest:web
        command: [apachectl, -DFOREGROUND]
    ```
4. depends_on
    - 특정 컨테이너에 대한 의존 관계를 나타내며 이 항목에 명시된 컨테이너가 먼저 생성되고 실행됨
    - links도 depends_on과 같이 컨테이너의 생성 순서와 실행 순서를 정의하지만 depends_on은 서비스 이름으로만 접근할 수 있다는 점이 다름  
    ```yaml
    services:
      web:
        image: alicek106/composetest:web
        depends_on:
          - mysql
      mysql:
        image: alicek106/composetest:mysql
    ```
    - 위 yaml파일을 실행하면 web은 mysql이 생성된 후에 생성되며 mysql 이라는 서비스명으로 mysql 도커 컨테이너에 접근 가능
    > 특정 서비스의 컨테이너만 생성하되 의존성이 이벗는 컨테이너를생성하려면 --no-deps 옵션 사용
    >```sh 
    >$ docker-compose up --no-deps web
    >```

    >links나 depends_on을 사용했더라도 컨테이너의 실행 순서만을 정할뿐 컨테이너 내부의 어플리케이션이 준비된상태인지까지는 확인하지 않음  
    따라서 디펜던시가 있는 컨테이너가 준비된 상태인지 확인하는 쉘 스크립트를 entrypoint로 지정하여 다른 컨테이너에 어플리케이션이 정상적으로 떴는 지 확인하는 방법을 사용할 수 있음  
    ><br/>
    >YAML 파일
    >```yaml
    >services:
    >  web:
    >   ...
    >   entrypoint: ./sync_script.sh mysql:3306
    >```
    ><br/> 
    >
    >sh파일  
    >```sh
    >until (상태를 확인할 수 있는 명령어); do
    >   echo "depend container is not available yet"
    >   sleep 1
    >done
    >echo "depends_on container is ready"
    >```
5. ports
    - docker run 명령어의 -p와 같으며 서비스의 컨테이너를 개방할 포트를 설정  
    - 단일 호스트 환경에서 80:80과 같이 호스트의 특정 포트를 서비스의 컨테이너에 연결하면 `docker-compose scale` 명령어로 서비스의 컨테이너의 수를 늘릴 수 없음  
    ```yaml
    services:
      web:
        image: alicek106/composetest:web
        ports:
          - "8080"
          - "8081-8085"
          - "80:80"
    ```
6. build
  - build 항목에 정의된 Dockerfile에서 이미지를 빌드해 서비스의 컨테이너를 생성하도록 설정  
  - `/composetest` 디렉터리에 저장된 Dockerfile로 이미지를 빌드해 컨테이너를 생성하는 yaml 파일 예시
    ```yaml
    services:
      web:
        build: ./composetest
        image: alicek106/composetest:web
    ```
  - 새롭게 빌드되는 이미지의 이름은 image 항목에 정의된 이름인 `alicek106/dockercomposetest:web`이 됨
  - build 항목에서는 Dockerfile에 사용될 context나 Dockerfile의 이름, Dockerfile에서 사용될 인자 값을 설정할 수 있음  
    ```yaml
    services:
      web:
        build: ./composetest
        context: ./composetest
        dockerfile: myDockerfile
        args:
          HOST_NAME: web
          HOST_CONFIG: self_config
    ```
  - 이미지 항목을 설정하지 않으면 이미지의 이름은 [프로젝트이름]:[서비스이름]이 됨   
  - `docker-compose up`을 할 때 build 항목의 이미지를 새로 빌드해 서비스를 올리고 싶을 때 명령어
    ```sh
    $ docker-compose up -d --build
    ```
  - yaml 파일의 서비스 이미지를 build하고 싶을 때 명령어
    ```sh
    $ docker-compose build [yml파일에서 빌드할 서비스 이름]
    ```
7. extends
  - 다른 yaml파일이나 현재 yaml 파일에서 서비스 속성을 상속받게 설정  
  - docker-compose.yml 파일
    ```yaml
    version: '3.0'
    services:
      web:
        extends:
          file: extend_compose.yml
          service: extend_web
    ```
  - extend-compose.yml
    ```yaml
    version: '3.0'
    services:
      extend_web:
        image: ubuntu:14.04
        ports:
          - "80:80"
    ```
  - 위 상황에서 docker-compose.yml 파일은 extend-compose.yml 파일의 extend_web 서비스를 상속받기 때문에 web 서비스의 컨테이너는 ubuntu:14.04 이미지를 사용하고 80번 포트를 호스트의 80번 포트로 포워딩하게 됨  
  - 같은 yml 파일에 있는 다른 서비스를 상속받는 것도 가능  
    ```yml
    version: '3.0'
    services:
      web:
        extends:
          services: extend-web
        extend_web:
          image: ubuntu:14.04
          ports:
            - "80:80"
    ```
  - 그러나 `depends_on`과 `links`, `volume_from` 항목은 각 컨테이너 사이의 의존성을 내포하고 있으므로 extends로 상속받을 수 없음  

### 네트워크 정의  
1. driver
     - 도커 컴포즈는 기본적으로 bridge 타입의 네트워크 생성  
     - 그 외의 다른 타입의 네트워크를 사용하려면 아래와 같이 설정
      ```yaml
      version: '3.0'
      services:
        myservice:
          image: nginx
          networks:
            - mynetwork
      networks:
        mynetwork:
          driver: overlay   # overlay 네트워크 사용 (스웜모드나 주키퍼를 사용하는 환경에서만 overlay 네트워크 사용 가능)
          driver_opts:      # 드라이버에 필요한 옵션들을 하위 항목으로 전달
            subnet: "255.255.255.0"
            IPAddress: "10.0.0.2"
      ```
2. ipam
    - IPAM(IP Address Manager)을 위해 사용할 수 있는 옵션을오 subnet, ip 범위 등을 설정할 수 있음  
    - driver 항목에는 IPAM을 지원하는 드라이버의 이름을 입력  
    ```yaml
    services:
      ...
    networks:
      ipam:
        driver: mydriver
        config:
          subnet: 172.20.0.0/16
          ip_range: 172.20.5.0/24
          gateway: 172.20.5.1
    ```
3. external
    - YAML 파일을 통해 프로젝트를 생성할 떄마다 네트워크를 생성하는 것이 아닌 기존의 네트워크를 사용하도록 설정  
    - 이를 설정하기 위해서는 외부 네트워크의 이름을 하위 항목으로 입력한 후에 external 값을 true로 설정  
    - external 옵션은 준비된 네트워크를 사용하므로 driver, driver_ops, ipam 옵션과 함께 사용 불가  
    ```yaml
    services:
      web:
        image: alicek106/composetest:web
        networks:
          - alicek106_network
    networks:
      alicesk106_network:
        external: true
    ```

### 볼륨 정의  
1. driver
    - 볼륨을 생성할 때 사용될 드라이버를 설정. 어떤 설정도 하지 않으면 local로 설정됨  
    - 드라이버를 사용하기 위한 추가 옵션은 하위 항목인 `driver_opts`를 통해 인자로 설정  
    ```yaml
    version: '3.0'
    services:
      ...
    volumes:
      driver: flocker
        driver_opts:
          opt: "1"
          opt2: 2
    ```
2. external
    - 도커 컴포즈는 YAML 파일에서 volume, volumes-from 옵션 등을 사용하면 프로젝트마다 볼륨을 생성함  
    - external 옵션을 설정하면 볼륨을 프로젝트를 생성할 때마다 매번 생성하지 않고 기존 볼륨을 사용하도록 설정
    ```yaml
    service:
      web:
        image: alicek106/composetest:web
        volumes:
          - myvolume:/var/www/html
    volumes:
      myvolume:
        external: true
    ```
### YAML 파일 검증하기
yaml 파일 작성 시 오타 검사나 파일 포맷이 적절한지 등을 검사하려면 `docker-compose config` 명령어를 사용함  
기본적으로 현재 디렉터리의 `docker-compose.yml` 파일을 검사하지만 `docker-compose -f (yml 파일 경로) config`와 같이 검사할 파일의 경로를 설정 가능  
![](/docker-compose/about-docker-compose/screenshots/docker_compose_config.png)