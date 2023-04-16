# 도커 스웜  

## 도커 스웜 사용 이유
하나의 호스트 머신에서 도커 엔진을 구동하다가 CPU나 메모리, 디스크 용량 등의 자원이 부족하면 더 좋은 스펙의 서버로 스케일업 하는 방법도 있지만, 가장 많이 사용하는 방법은 여러 대의 서버를 클러스터로 만들어 자원을 병렬로 확장하는 것임  

여러 대의 서버를 사용했을 때 해결해야 하는 부분
1. 새로운 서버나 컨테이너가 추가됐을 때 이를 발견(Service Discovery)하는 작업 필요  
2. 어떤 서버에 컨테이너를 할당할 것인가에 대한 스케줄러, 로드 밸런서 문제  
3. 클러스터 내의 서버가 다운됐을 때 고갸용성(high availability)을 어떻게 보장할 지에 대한 문제  

위와 같은 문제들을 해결하기 위해 나온 오픈소스 솔루션의 대표적이 예시가 바로 쿠버네티스, 도커 스웜 등임  

## 스웜 모드
스웜 모드는 별도 설치 과정은 필요 X, 도커 엔진 자체에 내장되어 있음  

도커 스웜 클러스터 정보 확인 명령어
```sh
$ docker info | grep Swarm
```
![](/docker_swarm/screenshots/docker_info_grep_swarm.png)
지금까지 도커 엔진을 사용한 방법들은 모두 단일 도커 서버에서 사용된 것이므로 현재 스웜 모드 상태는 비활성화(inactive) 상태임  
> 서버 클러스터링을 할 때는 반드시 각 서버의 시각을 NTP 툴을 이용해 동기화해야 함.  
> 서버 간에 설정된 시간이 다를 경우 예상치 못한 오류 발생 가능  

### 도커 스웜 모드의 구조
스웜모드 구성
1. 매니저 노드: 워커노드를 관리하기 위한 도커 서버 (그러나 기본적으로 워커 노드의 역할을 포함하기 때문에 컨테이너가 매니저 노드에도 생성될 수 있음)
2. 워커 노드: 실제로 컨테이너가 생성되고 관리되는 도커 서버

참고사항
1. 매니저 노드가 워커 노드의 역할도 포함하고 있어 매니저 노드만으로 스웜 클러스터를 구성할 수 있기 떄문에 매니저 노드는 1개 이상이 있어야 하지만 워커 노드는 없을 수도 있음  
  - 그러나 일반적으로 워커 노드와 매니저 노드를 구분해서 사용하는 것을 권장
2. 운영 환경에서 스웜 모드로 도커 클러스터를 구성하려면 매니저 노드를 다중화 하는 것을 권장
  - 이렇게 하면 매니저의 부하를 분산하고 특정 매니저 노드가 다운됐을 때 정상적으로 스웜 클러스터를 유지할 수 있기 때문
  - 매니저 수를 늘린다고 해서 스웜 클러스터의 성능이 좋아지는 것은 아님  
3. 스웜모드는 매니저 노드의 절반 이상에 장애가 생겨 정상 작동이 불가능한 경우 장애가 생긴 매니저 노드가 복구될 때까지 클러스터 운영을 중단함  
  - 매니저 노드가 짝수 개로 구성된 경우: 네트워크 파티셔닝과 같은 현상이 발생했을 경우 운영이 중단되 수 있음  
  - 매니저 노드가 홀수 개로 구성된 경우: 과반수 이상이 유지되는 쿼럼(quorum) 매니저에서 운영을 계속할 수 있음  
  - 스웜 매니저는 가능한 홀수 개로 구성하는 것이 권장됨  
  > **네트워크 파티션**  
  > 컴퓨터 네트워크를 상대적으로 독립적인 서브넷 으로 분할하는 것  
  > 두 서브넷 사이의 네트워크 스위치 장치에 오류가 발생하여 서로 연결이 불가능하면 파티션이 발생함  
  > 노드는 다른 노드에 일정시간동안 접속할 수 없으면 해당 노드가 작동 중지되었다고 판단하고 각각의 사이드는 독립적으로 돌아가며 split-brain 문제가 생김  
  > > split-brain: 클러스터로 구성된 두 시스템 그룹 간 네트워크의 일시적 동시 단절 현상으로 클러스터 상의 모든 노드들이 자신을 primary로 인식함  
  > > split-brain은 각 시스템이 특정 리소스에 배타적 액세스 권한이 있다고 가정할 경우 발생하며 공유 디스크 데이터에 영향을 줄 수 있다는 문제점이 있음  

## 도커 스웜 모드 클러스터 구축  
### 스웜 클러스터 시작  
```sh
$ docker swarm init --advertise-addr 192.168.0.100
# --advertise-addr: 다른 도커 서버가 매니저 노드에 접근하기 위한 IP 주소
```
![](/docker_swarm/screenshots/swarm_init.png)

매니저 노드에 2개 이상의 네트워크 인터페이스 카드가 있을 경우 스웜 클러스터 내에서 사용할 IP 주소를 지정해야하며, Public IP를 `--advertise-addr`에 지정해야함  

출력 결과 중 `docker swarm join` 명령어는 새로운 워커 노드를 스웜 클러스터에 추가할 때 사용됨  
`--token` 옵션에 사용된 토큰값은 새로운 노드를 해당 클러스터에 추가하기 위한 비밀키
```sh
$ docker swarm join --token SWMTKN-1-53im9u1g872omr8slqykn4hhybyhwmma6gjq1g1o1relcjajp3-9odcz1ypsilcd83tslnmb1e2l 172.31.85.32:2377
```  
![](/docker_swarm/screenshots/docker_swarm_join.png)
> 스웜 매니저는 기본적으로 2377 포트 사용
> 노드 사이의 통신은 7946/tcp, 7946/udp 포트 사용
> 스웜이 사용하는 네트워크인 ingress 오버레이 네트워크는 4789/tcp, 4789/udp 포트 사용

*AWS ec2로 작업 시에 manager 노드를 띄운 서버의 2377 포트를 개방해야함*  
*보안 > 보안그룹 > 인바운드 규칙 > 인바운드 규칙 편집 > 0.0.0.0/0 소스 2377/tcp 개방* 

클러스터 노드 목록 확인 명령어   
```sh
$ docker node ls
```
![](/docker_swarm/screenshots/docker_node_ls.png)

매니저노드 종류
1. 일반 역할 매니저 노드
2. 리더 역할 매니저 노드
  - 다른 매니저 노드에 대한 데이터 동기화 관리 담당 
  - 항상 작동 필수
  - 리더 매니저 서버 다운되는 경우 매니저는 새로운 리더를 Raft Consensus 알고리즘을 이용하여 선출  

새로운 매니저 노드를 추가하려면 매니저 노드를 위한 토큰을 사용해 docker swarm join 명령어 사용  
```sh
# 매니저 노드 추가 토큰
$ docker swarm join-token manager # 결과로 토큰 출력
# 워커 노드 추가 토큰
$ docker swarm join-token worker
```
> 토큰이 노출되면 누구나 클러스터에 노드를 추가할 수 있게 되므로 보안 측면에서 문제가 될 수 있어 공개 X, 실제 운영 환경에서는 주기적으로 토큰 변경하는 것이 안전  
![](/docker_swarm/screenshots/docker_swarm_join-token.png)

토큰 갱신
```sh
$ docker swarm join-token --rotate manager
```
![](/docker_swarm/screenshots/docker_swarm_join-token--rotate_manager.png)

추가된 워커노드 삭제  
```sh
# 매니저 노드가 워커 노드의 상태를 Down으로 인지
$ docker swarm leave
```
![](/docker_swarm/screenshots/docker_swarm_leave.png)

```
# 실제 워커 노드 삭제
$ docker node ls
$ docker node rm <워커노드 hostname>    # 워커노드의 ID 일부를 사용해 제어 가능
```
![](/docker_swarm/screenshots/docker_ls_rm_ls.png)

매니저 노드는 `leave` 명령어에 `--force` 옵션을 추가행 삭제 가능  
```sh
$ docker swarm leave --force
```
![](/docker_swarm/screenshots/docker_swarm_leave--force.png)
매니저 노드를 삭제하면 해당 매니저 노드에 저장되어있던 클러스터의 정보도 삭제됨  
매니저 노드가 1개일 때 삭제하면 스웜 클러스터는 더이상 사용 불가  

```sh
# 워커모드 -> 매니저 모드로 변경  
$ docker node promote <워커노드 hostname>
# 매니저 모드 -> 워커모드로 변경 (매니저 노드가 1개일 때는 사용 불가)
$ docker node demote <manager node hostname>
```
![](/docker_swarm/screenshots/docker_promote_demote.png)

## 스웜 모드 서비스  
스웜모드에서 제어하는 단위는 컨테이너가 아닌 서비스(Service)   

### 서비스
- 같은 이미지에서 생성된 컨테이너의 집합
- 서비스를 제어하면 해당 서비스 내의 컨테이너에 같은 명령이 수행됨  
- 서비스 내에 컨테이너는 1개 이상 존재할 수 있으며 컨테이너들은 각 워커 노드와 매니저 노드에 할당됨 (이런 컨테이너들을 '태스크'라고 함)

1. 도커 이미지로 서비스 생성 후 컨테이너 수를 n개로 설정
2. 스윔 스케쥴러는 서비스의 정의에 따라 컨테이너를 할당할 적합한 노드를 설정하고 해당 노드에 컨테이너를 분산해서 할당함  
3. 함께 생성된 컨테이너를 replica라고 하며 서비스에 설정된 레플리카의 수만큼의 컨테이너가 스윔 클러스터 내에 존재  
4. 스웜은 서비스 내에 정의된 레플리카의 수만큼 컨테이너가 존재하지 않으면 새로운 컨테이너 레플리카를 노드에 생성  

롤링 업데이트: 서비스 내 컨테이너들의 이미지를 일괄적으로 업데이트해야할 때 컨테이너 이미지를 순서대로 변경해 서비스 자체가 다운되는 시간 없이 컨테이너의 업데이트르 진행

### 서비스 생성  
서비스 제어 명령어는 매니저 노드에서 사용 가능  

**서비스 생성 명령어**  
```sh
$ docker service create \
  ubuntu:14.04 \
  /bin/sh -c "while true; do echo hello world; sleep 1; done"
```  
![](2023-04-16-16-00-19.png)
![](/docker_swarm/screenshots/docker_service_create.png)
> 서비스 내의 컨테이너는 detached 모드로 실행되기 때문에 계속 동작하는 명령을 주지 않으면 컨테이너 내부를 차지하고 있는 프로세스가 없어 컨테이너가 정지될 것이고, 스웜 매니저는 서비스의 컨테이너에 장애가 생긴 것으로 판단해 컨테이너르 계속 반복 생성하게되므로 docker run -d 옵션을 통해 동작할 수 있는 이미지를 사용해야 함

**서비스 목록 확인 명령어**
```sh
$ docker service ls
```

**서비스 상세 정보 확인 명령어** 
```sh
$ docker service ps <서비스 이름>
```
서비스 내의 컨테이너 목록, 상태, 컨테이너 할당된 노드 위치 등을 파악 가능  
![](/docker_swarm/screenshots/docker_service_ps.png)

**서비스 삭제 명령어**  
```sh
$ docker service rm <서비스 이름>
```  
도커 서비스 삭제는 서비스의 상태와 상관 없이 서비스가 실행중이더라도 서비스의 컨테이너를 바로 삭제함   
![](/docker_swarm/screenshots/docker_service_ls_ps_rm.png)


### nginx 웹 서버 서비스 생성하기  
`docker service create` 명령어에 `--replica` 옵션을 추가해 서비스를 여러 개 띄우고, Nginx 웹 서버 이미지를 이용해 서비스를 외부에 노출하도록 하기  

2개의 레플리카 컨테이너를 정의하고 서비스의 이름을 myweb으로 설정하며 컨테이너의 80번 포트를 각 노드의 80번 포트로 연결  
```sh
$ docker service create --name myweb \
  --replicas 2 \
  -p 80:80 \
  nginx
```
![](/docker_swarm/screenshots/docker_service_create_nginx.png)

생성된 서비스는 `docker service ps myweb`으로 확인 가능  
![](/docker_swarm/screenshots/docker_service_ps_myweb.png)  
`ps`로 확인한 결과 manager노드와 worker 노드에 하나씩 생성되었음을 확인할 수 있음  
하지만 꼭 서비스가 생성된 노드의 주로소 접근해야만 Nginx 웹 서버에 접근할 수 있는 것은 아니고, `docker service create` 명령어에서 `-p 80:80`을 통해 스웜 클러스터 자체에 포트를 개방했다고 생각하면 쉽게 이해할 수 있음  
-> 같은 스웜 클러스터에 있는 어떤 노드의 주소든 `<노드주소>:80`으로 접근 가능  

![](/docker_swarm/screenshots/nginx_page.png)

**레플리카 수 늘리기**
컨테이너 수를 늘리는 명령어
```sh
$ docker service scale myweb=4
```
![](/docker_swarm/screenshots/docker_service_scale.png)
한 노드에 여러 컨테이너가 할당되어도 포트가 겹치는 문제가 생기지는 않음  
이는 각 컨테이너들이 호스트의 80번 포트에 연결된 것이 아니라 실제로는 각 노드의 80번 포트로 들어온 요청을 위 4개의 컨테이너 중 1개로 리다이렉트 하기 때문임  
따라서 각 호스트의 어느 노드로 접근하든 4개의 컨테이너 중 1개에 접근하게 됨  

> 스웜 모드는 라운드 로빈(round-robin) 방식으로 서비스 내에 접근할 컨테이너를 결정  
> 각 노드의 트래픽이나 자원 사용량 등을 고려해 로드밸런싱을 해야한다면 이 방식은 적합하지 않을 수 있음  

### global 서비스 생성하기  
서비스 모드 종류  
1. 복제 모드(replicated)
  - 레플리카 수를 정의해 그 만큼의 같은 컨테이너를 생성
  - 위의 nginx 서비스가 이에 해당  
  - 실제 서비스를 제공하기 위해 일반적으로 쓰이는 모드  
2. 글로벌(global) 모드
  - 스웜 클러스터 내에서 사용할 수 있는 모든 노드에 컨테이너를 하나씩 생성  
  - 레플리카 셋을 별도로 지정하지 않음  
  - 스웜 클러스터를 모니터링하기 위한 에이전트 컨테이너 등을 생성해야 할 때 유용

**글로벌 서비스 생성 명령어**
```sh
# mode 옵션의 기본 값은 복제 모드
$ docker service create --name global_web \
  --mode global \
  nginx
```
![](/docker_swarm/screenshots/docker_service_create_global_mode.png)
서비스 목록을 확인해본 결과 `MODE`는 `global`로 설정되어 있으며 각 노드에 컨테이너가 하나씩 생성되었음을 확인할 수 있음  

### 스웜 모드의 서비스 장애 복구  
스웜모드에서 복제모드로 설정된 서비스 컨테이너가 정지되거나 특정 노드가 다운되었을 때 스웜 매니저는 새로운 컨테이너를 생성해 이를 자동으로 복구함  
![](/docker_swarm/screenshots/docker_container_forced_stop.png)

> docker ps 같은 도커 클라이언트 명령어로도 스웜의 서비스로 생성된 컨테이너를 확인 가능  
> docker ps 명령어에 `--filter is-task=true`를 추가함녀 스웜 모드의 서비스에서 생성된 컨테이너만 출력할 수 있음  

특정 노드가 다운됐을 대도 위와 같은 방식으로 작동  
![](/docker_swarm/screenshots/container_auto_restart_when_node_is_down.png)

다운됐던 노드가 재시작되더라도 다른 노드에 이미 할당된 컨테이너가 해당 노드로 옮겨가진 않음 (재균형(rebalance)작업 발생X)  
따라서 다운됐던 노드를 다시 복구했을 때 서비스의 컨테이너 할당의 균형을 맞추기 위해서는 `scale` 명령어를 이용해 컨테이너 수를 줄이고 다시 늘려야함

### 서비스 롤링 업데이트  
롤링 업데이트를 통해 서비스의 중단 없이 컨테이너를 차례대로 새로운 버전으로 업데이트가 가능  
롤링 업데이트 테스트를 위해 아래 nginx:1.10 이미지를 사용하는 서비스 생성  
```sh
$ docker service create --name myweb2 \
  --replicas 3 \
  nginx:1.10
```

서비스 생성 후에 `docker service update` 명령어로 서비스의 이미지를 업데이터할 수 있음  
update 명령어를 사용함녀 생성된 서비스의 각종 설정을 변경할 수 있으며 `--image` 옵션을 통해 이미지 업데이트 가능
```sh
$ docker service update \
  --image nginx:1.11 \
  myweb2
```
![](/docker_swarm/screenshots/docker_service_update.png)
nginx:1.10과 nginx:1.11 이미지의 레이어 차이가 크지 않으므로 빠른 속도로 롤링 업데이트가 진행됨  

서비스 생성 시 롤링 업데이트의 주기, 업데이트를 동시에 진행할 컨테이너의 개수, 업데이트에 실패했을 대 어떻게 할 것인지 등을 설정할 수 있음  
아래 명령어는 레플리카를 10초 단위로 업데이트하며 업데이트 작업을 한 번에 2개의 컨테이너에 수행한다는 것을 의미함  
(따로 설정값이 없을 때는 주기 없이 차례대로 컨테이너를 한 개씩 업데이트함)  
```sh
$ docker service create \
  --replicas 4 \
  --name myweb3 \
  --update-delay 10s \
  --update-parallelism 2 \
  nginx:1.10
```
![](/docker_swarm/screenshots/rolling_update_inspect.png)

정보에서 `On failure` 가 `pause`로 설정되어 있는 것은 업데이트 도중 오류가 발생하면 롤링 업데이트를 중지하는 것을 의미함  
기본값은 `pause`이나 서비스 생성 시 `--update-failure-action continue`로 설정하면 오류가 발생해도 계속 롤링 업데이트를 진행하게 할 수 있음  

롤링 업데이트 후 서비스를 롤링 업데이트 전으로 되돌리는 롤백도 가능  
```sh
docker service rollback myweb2 
```
![](/docker_swarm/screenshots/docker_service_rollback.png)

### 서비스 컨테이너에 설정 정보 전달하기: config, secret
환경에 대한 설정 파일이나 값들을 컨테이너 내부에 미리 준비시켜두기 위해 
1. 이미지 내부에 정적으로 설정값을 저장
2. 설정값이 저장된 볼륨을 컨테이너에 마운트
3. 컨테이너 실행 시 `-e` 옵션을 통한 환경변수 설정  

등의 방법을 사용해왔지만, 서버 클러스터에서 파일 공유를 위해 설정 파일을 호스트마다 마련해두는 것은 비효율적인 일임  
또한 비밀번호 등 민감한 정보를 환경변수로 설정하는 것은 보안상 바람직하지 않음  

이를 위해 스웜 모드는 secret과 config라는 기능을 제공  
1. **secret**
  - 비밀번호나 SSH 키, 인증서 키와 같이 보안에 민감한 데이터를 전송하기 위해서 사용
2. **config**
  - nginx나 레지스트리 설정 파일과 같이 암호화할 필요가 없는 설정값들에 대해 쓰일 수 있음

> secret과 config는 스웜 모드에서만 사용 가능하며 docker run 명령어에서는 사용 불가  

**secret 사용하기**  
secret을 생성하여 `my_mysql_password` 라는 이름의 secret에 `1q2w3e4r`값을 저장하는 명령어  
```sh
$ echo 1q2w3e4r | docker secret create my_mysql_password -
```
![](/docker_swarm/screenshots/docker_secret_create.png)

secret 값은 매니저 노드 간에 암호화된 상태로 저장되므로 secret을 조회해도 실제 값을 확인할 수는 없음  
secret 파일은 컨테이너에 배포된 후에도 파일 시스템이 아닌 메모리에 저장되기 때문에 서비스 컨테이너가 삭제될 경우 secret도 함께 삭제되는 휘발성을 띠게 됨  

생성된 secret을 이용해 MySQL 컨테이너 생성하기  
```sh
$ docker service create \
  --name mysql \
  --replicas 1 \
  --secret source=my_mysql_password,target=mysql_root_password \
  --secret source=my_mysql_password,target=mysql_password \
  -e MYSQL_ROOT_PASSWORD_FILE="/run/secrets/mysql_root_password" \
  -e MYSQL_PASSWORD_FILE="/run/secrets/mysql_password" \
  -e MYSQL_DATABASE="wordpress" \
  mysql:5.7
```
`--secret` 옵션을 통해 컨텡너로 공유된 값은 기본적으로 컨테이너 내부의 `/run/secrets/` 디렉터리에 마운트됨  
위 명령어에서 target 값이 각각 `mysql_root_password`, `mysql_password`로 설정되었으므로 `/run/secrets/` 디렉터리에 해당 이름의 파일이 각각 존재함  
> target 값에 절대 경로를 입력해 `/run/secrets/` 이외의 다른 경로에 secret 파일을 공유할 수도 있음  
![](/docker_swarm/screenshots/docker_exec_mysql.png)

이런 식의 값 전달 시 주의할 점은 컨테이너 내부의 애플리케이션이 특정 경로의 파일 값을 참조할 수 있도록 설계해야 한다는 것임  
위의 예시에서 `-e MYSQL_PASSWORD_FILE="/run/secrets/mysql_password"` 처럼 옵션을 통해 특정 경로의 파일로부터 비밀번호를 로드할 수 있어야 함  


**config 사용하기**  
config 사용법은 secret과 거의 동일 

아래 내용의 config.yaml 파일을 config로 사용  
```yaml
# config.yml

version: 0.1
log:
  level: info     # 로그 출력 레벨 info로 설정
storage:
  filesystem:
    rootdirectory: /registry_data   # 이미지 파일이 저장되는 디레터리 지정
  delete:
    enabled: true
http:
  addr: 0.0.0.0:5000    # 레지스트리 서비스를 바인딩할 주소
```

아래 명령어로 config 생성
```sh
$ docker config create registry-config config.yaml
```
![](/docker_swarm/screenshots/create_config.png)

config는 secret과 달리 내용을 base64로 인코딩한 뒤 저장  
base64 명령어를 통해 디코딩하면 원래 값을 확인 가능  

![](/docker_swarm/screenshots/config_base64_decode.png)

앞의 config로 사설 레지스트리 생성하기  
```sh
$ docker service create --name yml_registry -p 5000:5000 \
  --config source=registry-config,target=/etc/docker/registry/config.yml \
  registry:2.6
```
![](/docker_swarm/screenshots/docker_exec_cat_config.png)

secret, config 값은 수정은 불가능하지만 서비스 컨테이너가 새로운 값을 사용해야한다면 `-config-rm`, `--config-add`, `--secret-rm`, `--secret-add` 옵션을 사용해 서비스가 사용하는 secret이나 config르 추가/삭제 가능  

## 도커 스웜 네트워크  
스웜모드는 일반적인 도커 네트워크 구조와는 조금 다른 방법을 이용함  

1. 스웜 모드는 여러 개의 도커 엔진에 같은 컨테이너를 분산시켜 할당하기 때문에 각 도커 데몬의 네트워크가 하나로 묶인 네트워크 풀이 필요
2. 서비스를 외부로 노출했을 때 어느 노드로 접근하더라도 해당 서비스의 컨테이너에 접근할 수 있게 라우팅 기능이 필요  

-> 이런 네트워크 기능은 스웜모드가 자체적으로 지원하는 네트워크 드라이버를 통해 사용 가능  

매니저 노드에서 `docker network ls` 명령어를 실행해 네트워크의 목록을 확인  
![](/docker_swarm/screenshots/docker_network_ls.png)

이전에 보지 못했던 `docker_gwbridge`, `ingress`를 확인할 수 있음  
**docker_gwbridge** 네트워크는 스웜에서 오버레이(overlay) 네트워크를 사용할 떄 사용됨  
**ingress** 네트워크는 로드 밸런싱과 라우팅 메시(Routing mesh)에 사용됨  

### ingress 네트워크  
스웜 클러스터를 생성하면 자동으로 등록되는 네트워크로 스웜 모드를 사용할 때만 유효함  
`docker network ls`로 네트워크 목록을 확인해보면 `ingress`의 `scope`가 `swarm`인 것을 확인 가능  
매니저 노드뿐 아니라 스웜 클러스터에 등록된 노드라면 모두 ingress 네트워크가 생성됨  

**ingress 네트워크의 역할**  
ingress network는 어떤 스웜 노드에 접근하더라도 서비스 내의 컨테이너에 접근할 수 있게 설정하는 라우팅 메시를 구성하고, 서비스 내의 컨테이너에 대한 접근을 라운드 로빈 방식으로 분산하는 로드 밸런싱을 담당함  

컨테이너의 호스트 이름(임의로 할당된 16진수)을 출력하는 PHP 파일이 들어있는 웹 서버 이미지로 컨테이너를 생성하여 ingress의 역할 알아보기  
```sh
$ docker service create --name hostname \
  -p 80:80 \
  --replicas=4 \
  alicek106/book:hostname
```

컨테이너의 호스트명 확인하기
![](/docker_swarm/screenshots/docker_ingress_ps1.png)
![](/docker_swarm/screenshots/docker_ingress_ps2.png)

<노드 즈소>:90 으로 컨테이너 접근 결과
![](/docker_swarm/screenshots/ingress_webpage2.png)
![](/docker_swarm/screenshots/ingress_webpage.png)
접근을 하다보면 서로 다른 호스트 이름 총 4가지가 출력되는 것을 확인할 수 있음  

스웜모드로 생성된 모든 서비스의 컨테이너가 외부로 노출되기 위해 무조건 ingress 네트워크를 사용해야 하는 것은 아님  
`docker run -p`를 이용해 외부에 컨테이너를 노출했던 것처럼 호스트의 특정 포트를 사용하도록 설정 가능  

ingress 네트워크를 사용하지 않고 호스트의 8080번 포트를 직접 컨테이너의 80번 포트에 연결하는 예  
```sh
$ docker service create \
  --publish mode=host,target=80,published=8080,protocol=tcp \
  --name web \
  nginx
```
그러나 ingress를 사용하지 않으면 어떤 호스트에서 컨테이너가 생성될 지 알 수 없어 포트 및 서비스 관리가 쉽지 않다는 단점이 있음  
(가급적이면 ingress 네트워크 이용해 외부로 서비스 노출하기)  

### 오버레이 네트워크
스웜 클러스터 내의 컨테이너가 할당받는 IP 주소 확인하기  
```sh
$ docker ps --format "table {{.ID}}\t{{.Status}}\t{{.Image}}"
$ docker exec 6f8f89f2e778 ifconfig
$ docker exec 2b4c0a586d78 ifconfig
```
![](/docker_swarm/screenshots/docker_exec_ifconfig1.png)
![](/docker_swarm/screenshots/docker_exec_ifconfig2.png)  

컨테이너마다 eth0, eth1, lo가 할당됐고, eth0이 ingress 네트워크와 연결된 네트워크 카드   

ingress 네트워크는 오버레이 네트워크 드라이버를 사용  
오버레이 네트워크는 여러 개의 도커 데몬을 하나의 네트워크 풀로 만드는 네트워크 가상화 기술의 하나로서, 도커에 오버레이 네트워크를 적용하면 여러 도커 데몬에 존재하는 컨테이너가 서로 통신할 수 있음  

1. 여러 노드에 각각의 도커 데몬이 있음
2. 각 스웜 노드에 있는 도커 데몬들은 오버레이 네트워크 가상화 기술을 통해 하나의 네트워크 풀을 구성
3. 오버레이 네트워크를 적용하여 각 노드의 컨테이너 간의 통신이 가능  
4. 여러 개의 스웜 노드에 할당된 컨테이너들은 오버레이 네트워크의 서브넷에 해당하는 IP 대역을 할당받고 이 IP를 할당받고 이 IP를 통해 서로 통신 가능  
> swarm-manager의 컨테이너는 별도의 포트 포워딩을 설정하지 않아도 swarm-worker1의 컨테이너에 ping을 보낼 수 있음  

![](/docker_swarm/screenshots/docker_overlay_ping.png)
>10.0.0.24 = 같은 노드에 위치   
>10.0.0.25 = 다른 노드에 위치   
>서로 다른 노드에 있어도 ping 전송 가능

*앞서 봤던 MacVLAN도 다른 호스트의 컨테이너 간 통신이 가능하다는 점에서 오버레이 네트워크와 기능상으로는 비슷하다고 볼 수 있음.*  
*때문에 MacVLAN도 스웜 모드의 서비스의 컨테이너에 적용해 사용할 수 있음*
  

### docker_gwbridge 네트워크  
오버레이 네트워크를 사용하지 않는 컹테이너는 기본적으로 존재하는 bridge 네트워크를 사용해 외부와 연결   
그러나 ingress를 포함한 모든 오버레이 네트워크는 이와 다른 브리지 네트워크인 docker_gwbridge 네트워크와 함께 사용됨  
docker_gwbridge 네트워크는 외부로 나가는 통신 및 오버레이 네트워크의 트래픽 종단점 역할을 담당  
docker_gwbridge 네트워크는 컨테이너 내부의 네트워크 인터페이스 카드 중 eth1과 연결  

### 사용자 정의 오버레이 네트워크  
스웜모드는 자체 키-값 저장소를 갖고 있으므로 별도의 구성 없이 사용자 정의 오버레이 네트워크를 생성 및 사용 가능  

사용자 정의 오버레이 네트워크 생성 입력어
```sh
$ docker network create \
  --subnet 10.0.9.0/24 \
  -d overlay \
  myoverlay
```
![](/docker_swarm/screenshots/create_overlay.png)
> 오버레이 네트워크를 생성하는 명령어는 하나의 노드에서 입력해도 클러스터에 속한 다른 노드들에 자동으로 적용됨  
> 오버레이 네트워크가 적용되는 시점은 생성되었을 때가 아니라 해당 오버레이 네트워크를 사용하는 서비스의 컨테이너가 할당될 때임  

새로 생성된 오버레이 네트워크의 `SCOPE`가 `swarm`이라는 것은 스웜 클러스터에서만 사용할 수 있는 네트워크라는 것을 의미  
-> 매니저 노드에서 `docker service create` 명령어를 통해서만 이 네트워크를 사용하는 서비스를 생성 가능  
(`docker run` 명령어로는 해당 네트워크를 사용하는 서비스 생성 불가)

`docker run --net` 명령어로 스웜 모드의 오버레이 네트워크를 사용하려면 네트워크 생성 시에 --attachable을 추가해야함  
```sh
$ docker network create -d overlay \
  --attachable \
  myoverlay2
```
![](/docker_swarm/screenshots/create_overlay_network_--attachable.png)   

도커 서비스 생성 시에는 `docker service create` 명령어에 `--network` 옵션을 추가해 네트워크를 서비스에 적용해 컨테이너 생성 가능  
```sh
$ docker service create --name overlay_service \
  --network myoverlay \
  --replicas 2 \
  alicek106/book:hostname
```
![](/docker_swarm/screenshots/service_use_overlay_without_portforwarding.png)

생성된 컨테이너에서 `ifconfig`로 네트워크 인터페이스를 확인해보면 eth0에 오버레이 네트워크의 IP 주소가 할당된 것을 확인 가능  
(`--subnet 10.0.9.0/24` 옵션을 통해 서브넷을 지정했고, 해당 서브넷 이하의 주소가 할당되었음)  
docke service create 명령어에서 -p 옵션을 사용하지 않으면 서비스를 외부로 노출하지 않게되고 ingress 네트워크를 사용하도록 설정되지 않음  

사용자 정의 오버레이 네트워크도, ingress 네트워크도 사용하지 않으면 docker_gwbridge 네트워크도 사용하지 않고 대신 기본 bridge 네트워크를 사용  
-> 컨테이너 내부에 `172.17.0.X` 대역의 인터페이스 하나만 존재  

## 서비스 디스커버리
같은 컨테이너를 여러 개 만들어 배포할 때 새로 생성된 컨테이너가 생성되었는 지 발견하는 것 (Service Discovery)
와 없어진 컨테이너의 감지가 중요   
일반적으로 이 역할은 주키퍼, etcd 등 분산 코디네이터를 외부에 두고 사용해서 해결하나 스웜 모드는 서비스 발견 기능을 자체 지원함  

도커에서 A 서비스가 B 서비스를 사용한다고 할 때 B 서비스의 replica를 늘려도 A 서비스는 'B'라는 이름으로 B의 컨테이너에 접근할 수 있기 때문에 B 컨테이너의 IP 주소를 알아야하거나 생성 사실을 알아야 할 필요가 없음  

스웜모드의 서비스 디스커버리는 오버레이 네트워크를 사용하는 서비스에 대해 작동하므로 오버레이 네트워크 생성  
```sh
$ docker network create -d overlay discovery
```

2개의 서비스를 생성  
- server 서비스: 컨테이너의 호스트 이름을 출력하는 웹 서버 컨테이너 2개 생성
- client 서비스: server 서비스에 http 요청을 보낼 컨테이너 생성
```sh
$ docker service create --name server \
  --replicas 2 \
  --network discovery \
  alicek106/book:hostname
```

```sh
$ docker service create --name client \
  --network discovery \
  alicek106/book:curl \
  ping docker.com
```

생성된 client 서비스 컨테이너로 접근  
![](/docker_swarm/screenshots/ping_from_client_to_server_replica_2.png)   
컨테이너 내부에서 curl 명령어를 이용해 server에 접근한 결과 명령어를 보낼 때마다 다른 컨테이너에 접근하는 것을 확인 가능  
server라는 호스트 이름을 사용함녀 이는 자동으로 server 서비스의 컨테이너 IP 중 하나로 변환되며 IP를 반환할 때는 라운드 로빈 방식을 따름  
서비스의 이름은 오버레이 네트워크에 해당 서비스가 속한다면 도커 스웜의 DNS가 이를 자동으로 변환하기 때문에 `curl -s server` 형식의 명령어를 보내면 알아서 IP로 변환해주는 것임  

컨테이너에서 나온 후 server replica를 3개로 증가시키고 다시 curl요청을 보낸 결과 별 다른 일을 하지 않아도 3개의 컨테이너에 다 접근되는 것을 확인 가능  
![](/docker_swarm/screenshots/ping_from_client_to_server_replica_3.png)  

server라는 서비스 이름이 각 컨테이너의 IP로 변환되는 이유  
- server라는 호스트 이름이 3개의 ip를 가지는 것이 아니라 서비스의 VIP(Virtual IP: 가상 IP)를 가지는 것임  
- server 서비스의 VIP 확인 명령어  
  ```sh
  docker service inspect --format {{.Endpoint.VirtualIPs}} server
  ```
  ![](/docker_swarm/screenshots/insepct_service_virtual_IP.png)
- 스웜모드가 활성화되면 내장 DNS 서버는 server라는 호스트 이름을 10.0.2.2라는 IP로 변환함  
- client가 server라는 호스트 이름으로 접근 = 실제로는 10.0.2.2로 요청을 전송  
- 10.0.2.2라는 IP는 컨테이너의 네트워크 네임스페이스 내부에서 실제 server 서비스의 컨테이너의 IP로 포워딩됨  

VIP 방식이 아닌 도커 내장 DNS 서버를 기반으로 라운드 로빈을 사용할 수 있음  
``docker service create` 옵션으로 `--endpoint-mode dnsrr`을 추가하면 됨  
```sh
$ docker service create --name server2 \
  --replicas 2 --network discovery \
  --endpoint-mode dnsrr \
  alicek106/book:hostname
```
그러나 이 방식은 애플리케이션에 따라 캐시 문제로 인해 서비스 발견이 정상적으로 작동하지 않을 때가 있으므로 가급적 VIP를 사용하는 것이 좋음  
