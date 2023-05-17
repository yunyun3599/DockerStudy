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
    - 컨테이너 내부에서 사용할 환경변수를 지정하며, Dictionary나 배열 형태로 사용 가능
    ```yaml
    services:
      web:
        environment:
          - MYSQL_ROOT_PASSWORD=mypassword
          - MYSQL_DATABASE_NAME=mydb
    ```
    ```yaml
    services:
      web:
        environment:
          MYSQL_ROOT_PASSWORD: mypassword
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
    > 특정 서비스의 컨테이너만 생성하되 의존성이 없는 컨테이너를생성하려면 --no-deps 옵션 사용
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
    - IPAM(IP Address Manager)을 위해 사용할 수 있는 옵션으로 subnet, ip 범위 등을 설정할 수 있음  
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

## 도커 컴포즈 네트워크  
YAML 파일에 네트워크 항목을 정의하지 않으면 도커 컴포즈는 프로젝트별로 브리지 타입의 네트워크를 생성함  
생성된 네트워크의 이름은 `{프로젝트이름}_default`로 설정되며, `docker-compose up` 명령어로 생성되고 `docker-compose down` 명령어로 삭제됨  
![](/docker-compose/about-docker-compose/screenshots/docker-compose-default_network.png)  

`docker-compose scale` 명령어로 생성되는 컨테이너 전부가 이 브리지 타입의 네트워크를 사용함  
서비스 내의 컨테이너는 `--net-alias`가 서비스 이름을 갖도록 자동으로 설정되므로 이 네트워크에 속한 컨테이너는 서비스의 이름으로 서비스 내의 컨테이너에 접근 가능  
![](/docker-compose/about-docker-compose/screenshots/docker-compose_find_by_service_name.png)
위의 그림처럼 web 서비스의 컨테이너가 mysql 이라는 호스트 이름으로 접근하면 mysql 서비스의 컨테이너 중 하나의 IP로 변환되며 컨테이너가 여러 개 존재할 경우 라운드 로빈으로 연결을 분산함  

## 도커 스웜 모드와 함께 사용하기
+ 도커 컴포즈 1.10 버전 
+ 스웜모드와 함께 사용 가능한 YAML 버전 3 배포 
+ 스웜 모드와 함께 사용되는 개념인 stack이 도커 엔진 1.13 버전에 추가

> stack: YAML 파일에서 생성된 컨테이너의 묶음
> - YAML 파일에 정의된 서비스가 스웜 모드의 서비스로 변환된 것  
> - YAML 파일로 스택 생성 = YAML 파일에 정의된 서비스가 스웜모드의 클러스터에서 생성됨   


스택은 도커 컴포즈 명령어인 `docker-compose`가 아닌 `docker stack`으로 제어해야 함 (스택은 도커 컴포즈에 의해서 생성된 것이 아니라 스웜 모드 클러스터의 매니저에 의해 생성된 것이기 때문)   
스택을 생성하고 삭제하는 작업은 스웜 매니저에서만 수행 가능  

### 스택 사용하기  
예시 docker-compose.yml 파일
```yml
version: '3.0'

services:
  web:
    image: alicek106/composetest:web
    command: apachectl -DFOREGROUND
    links: 
      - mysql:db
    ports:
      - 80:80
  mysql:
    image: alicek106/composetest:mysql
    command: mysqld
```

**스택 생성 명령어**  
```sh
# docker stack deploy -c [YAML 파일] [스택 이름]
# docker stack deploy --config-file [YAML 파일] [스택 이름]
$ docker stack deploy -c docker-compose.yml mystack
```
![](/docker-compose/about-docker-compose/screenshots/docker_stack_deploy.png)

수행 결과의 `Ignoring unsupported options: links`에서 알 수 있듯 links나 depends_on과 같이 컨테이너 간의 의존성을 정의하는 항목은 사용 불가  
-> links를 사용하려면 양 컨테이너가 같은 호스트에 생성되어야 하기 때문  

**생성된 스택 확인 명령어**  
```sh
# 스택 확인 명령어
$ docker stack ls

# stack ps 명령어
$ docker stack ps mystack

# 스웜모드 서비스 명령어
$ docker service ls

# 스택 서비스 명령어
$ docker stack services mystack
```
![](/docker-compose/about-docker-compose/screenshots/docker_stack_ls_ps_service_ls.png)
스택에서 생성된 서비스는 스웜모드에서 사용된 서비스와 같기 때문에 `docker service ls`와 `docker stack services mystack`의 결과로 출력되는 서비스는 동일  

스택은 docker compose가 아니라 스웜 킷에 의해 생성되기 때문에 `docker-compose scale` 명령어가 아니라 `docker service scale` 명령어를 사용해 컨테이너 수를 조절해야 함   

**스택 개수 조정 명령어**  
```sh
$ docker service scale mystack_web=2
```
![](/docker-compose/about-docker-compose/screenshots/docker_service_scale.png)

**스택 삭제 명령어**
```sh
$ docker stack rm mystack
```
![](/docker-compose/about-docker-compose/screenshots/docker_stack_rm.png)  

### 스택 네트워크  
도커 컴포즈에서 별 다른 설정 없이 프로젝트를 생성했을 때 네트워크가 자동으로 생성된 것 처럼 스택도 해당 스택을 위한 네트워크가 자동으로 생성됨  

![](/docker-compose/about-docker-compose/screenshots/docker_stack_deploy.png)
위 로그의 `Creating network mystack_default` 에서 네트워크가 생성되었음을 확인 가능  

네트워크 목록을 확인해보면 다음과 같음  
![](2023-05-04-23-01-56.png)
스택의 네트워크는 기본적으로 오버레이 네트워크 속성을 가지며 스웜 클러스터에서 사용되도록 설정됨   
*(docker-compose의 기본 생성 네트워크는 bridge 타입이었음)*  
또한 `SCOPE`가 `swarm`으로 설정되어있고 `--attachable` 옵션이 설정되지 않기 때문에 일반 컨테이너는 이 네트워크를 사용할 수 없음  
<br><br>

# 도커 마무리  
**OCI (Open Container Initiative)**
- 컨테이너의 표준 정의
- 컨테이너를 구성하기 위해 공통적으로 구현돼야 하는 런타임 및 이미지 스펙의 표준을 정의  

OCI 발표 이후 Moby라는 프로젝트에서 도커 컨테이너 기술을 관리하기 시작했고, 도커는 runC와 containerd, 도커 엔진으로 분리됨  
![](/docker-compose/about-docker-compose/screenshots/docker_structure.png)

1. 도커 데몬
    - 컨테이너 아님
    - containerd와 통신해 runC를 사용할 수 있도록 하는 엔드 유저용 도구
2. containerd
    - 여러 개의 runC 컨테이너 프로세스 및 이미지를 관리하는 주체
3. runC
    - 실제 컨테이너 프로세스
    - 컨테이너에 1:1로 매칭되는 런타임 역할

runC와 containerd는 도커 엔진 없이도 독립적으로 사용할 수 있으므로 컨테이너를 생성하고 사용하기 위해 도커가 반드시 필요한 것은 아님  
따라서 흔히 '도커 컨테이너'라고 불리는 것은 엄밀히 말하면 '도커'가 아니지만 일반적으로 도커 엔진과 runC, containerd를 함께 사용하는 경우가 많으므로 통상적으로 '도커 컨테이너'라고 부르는 경우가 많음  

> 추가적으로 runC와 containerd 외에도 다양한 컨테이너들이 존재함  
> - kata 컨테이너: 호스트와의 격리 수준을 높임
> - firecracker: AWS에서 개발
> - cri-o / Podman: 쿠버네티스 생태계에서 사용 (cri-o: containerd / Podman: 도커 엔진에 대응)
