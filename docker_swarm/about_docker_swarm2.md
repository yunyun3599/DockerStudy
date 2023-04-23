# 도커 스웜2   

## 스웜 모드 볼륨  
기존 도커 데몬의 경우 run 명령어에서 -v 옵션을 주어 볼륨을 사용했는데 로컬 볼륨을 사용할 때와 도커 볼륨을 사용할 때 그 사용 형식에 큰 차이가 없었음  
```sh
# 호스트 디렉터리 마운트 
$ docker run -ti --name host_volume -v /root:/root ubuntu14:04
# 도커 볼륨 사용
$ docker run -ti --name docker_volume -c myvolume:/root ubuntu:14:04
```

스웜모드의 도커 역시 볼륨 마은트를 `-v` 옵션을 주어 수행하나 도커 볼륨을 사용하는 경우와 호스트와 디렉터리를 공유할 지를 좀 더 명확히하여 볼륨을 사용  
-> 서비스 생성 시에 도커 볼륨을 사용할 지 호스트와 디렉터리를 공유할 지 명시

### volume 타입의 볼륨 생성  
스웜 모드에서 도커 볼룸을 사용하는 서비스를 생성하기 위해서는 `--mount` 옵션의 type 값에 volume을 지정함  
```sh
# source=사용할 볼륨, target=컨테이너 내부에 마운트될 디렉터리 위치
# source에 해당하는 이름읠 볼륨이 있으면 해당 볼륨을 사용하고 없으면 새로 생성
$ docker service create --name ubuntu \
  --mount type=volume,source=myvol,target=/root \
  ubuntu:14.04 \
  ping docker.com

# source 옵션을 명시하지 않으면 16진수로 구성된 익명의 이름을 가진 볼륨을 생성  
$ docker service create --name ubuntu2 \
  --mount type=volume,target=/root \
  ubuntu:14.04 \
  ping docker.com
```
![](/docker_swarm/screenshots/docker_create_service_mount_volume.png)

서비스 컨테이너에서 볼륨에 공유할 컨테이너의 디렉터리 파일이 이미 존재하면 이 파일들은 볼륨에 복사되고, 호스트에서 별도의 공간을 차지하게 됨  
그러나 서비스를 생성할 때 볼륨 옵션에 volume-nocopy를 추가하면 컨테이너의 파일들이 볼륨에 복사되지 않도록 설정할 수 있음  

다음 명령어로 서비스를 생성하면 서비스를 위한 새로운 볼륨을 생성함과 동시에 컨테이너 내부의 `/etc/vim` 디렉터리에 있는 파일을 볼륨으로 복사함  
```sh
$ docker service create --name ubuntu \
  --mount type=volume,source=test,target=/etc/vim \
  ubuntu:14.04 \
  ping docker.com

$ docker run --name test -v test:/root ubuntu:14.04 ls root/
```
위에서 서비스를 create 하면서 생성된 volume을 마운트시킨 컨테이너에서 마운트된 경로의 목록을 확인해보면 `/etc/vim` 위치의 파일이 잘 들어가있음을 확인할 수 있음  
![](/docker_swarm/screenshots/docker_service_mount_copy.png) . 

볼륨에 파일이 복사되는 것을 방지하는 옵션을 주어 위와 같은 명령을 다시 실행하면 결과는 아래와 같음  
```sh
$ docker service create --name ubuntu \
  --mount type=volume,source=test,target=/etc/vim,volume-nocopy \
  ubuntu:14.04 \
  ping docker.com

$ docker run --name test -v test:/root ubuntu:14.04 ls root/
```
![](/docker_swarm/screenshots/docker_service_mount_nocopy.png)

### bind 타입의 볼륨 생성  
바인드 타입은 호스트와 디렉터리를 공유할 때 사용됨  
공유될 호스트의 디렉터리를 설정해야하므로 source 옵션을 반드시 명시해야함  
바인드 타입은 type옵션의 값을 `bind`로 설정해서 사용  
```sh
$ docker service create --name ubuntu \
  --mount type=bind,source=/home/ubuntu,target=/root/container \
  ubuntu:14.04 \
  ping docker.com
```
![](/docker_swarm/screenshots/docker_mount_host_bind.png)

### 스웜 모드에서 볼륨의 한계점  
스웜 클러스터에서 볼륨을 사용하는 것은 굉장히 까다로운데 서비스를 할당 받을 수 있는 모든 노드가 볼륨 데이터를 가지고 있어야하기 떄문  
따라서 여러 개의 도커 데몬을 관리해야 하는 스웜 모드에서는 도커 볼륨이나 호스트와의 볼륨 사용이 적합하지 않을 수 있음  

PaaS (Platform as a Service) 같은 시스템을 구축하려고 하면 이는 더 큰 문제점이 됨  
어느 노드에 컨테이너를 할당해도 볼륨을 사용할 수 있는 방법은 모든 노드에 같은 데이터의 볼륨을 구성하는 것이지만 이는 별로 좋은 방법이 아님  
![](/docker_swarm/screenshots/saas_paas_iaas.png) . 

이런 문제점을 해결하기 위한 일반적인 방법은 어느 노드에서도 접근 가능한 퍼시스턴트 스토리지(Persistent Storage)를 사용하는 것임   
퍼시스턴트 스토리지는 호스트와 컨테이너와는 별개로 외부에 존재해 네트워크로 마운트할 수 있는 스토리지임   

퍼시스턴트 스토리지를 사용하면 컨테이너가 어느 노드에 할당되든 컨테이너에 필요한 파일을 읽고 쓸 수 있음  
그러나 퍼시스턴트 스토리지는 도커가 자체적으로 제공해주는 기능은 아니므로 서드파티 플러그인을 사용하거나 nfs, dfs 등을 별도로 구성해야 함  

혹은 노드에 라벨(label)을 붙여 서비스에 제한을 거는 방법도 있음  
= 필요한 볼륨이 존재하는 노드에 라벨을 붙여 해당 라벨이 있는 노드에만 컨테이너를 할당하게 설정하는 방법  

## 도커 스웜 모드 노드 다루기  
현재는 스웜 모드의 스케줄러를 사용자가 수정할 수 있게 자체적으로 제공하지 않기 때문에 별도의 스케줄링 전략을 세울 수는 없음  
그러나 스웜 모드가 제공하는 기본 기능만으로 어느정도의 전략은 수립 가능  

### 노드 AVAILABILITY 변경하기  
현재 노드 상태 확인  
![](/docker_swarm/screenshots/docker_node_ls_availability.png)   

일반적으로 매니저와 같은 마스터 노드는 최대한 부하를 받지 않도록 서비스를 할당받지 않게 하는 것이 좋음  
또한 특정 노드에 문제가 발생해 유지보수 작업을 수행할 때 해당 노드에 컨테이너를 할당하지 않게 하고 싶을 수도 있음  
이를 위해 특정 노드의 AVAILABILITY를 설정하여 컨테이너의 할당 가능 여부를 변경 가능  

#### **Active**  
새로운 노드가 스웜 클러스터에 추가되면 기본적으로 설정되는 상태   
노드가 서비스의 컨테이너를 할당받을 수 있음을 의미  
Active 상태가 아닌 노드를 Active 상태로 변경하려면 다음과 같이 docker node update 명령어를 사용  
```sh
$ docker node update \
  --availability active \
  adwqqmreiawyyzar22iuw93wo
```
![](/docker_swarm/screenshots/docker_node_update_active.png)

#### **Drain**
노드를 Drain 상태로 설정하면 스웜 매니저의 스케줄러는 컨테이너를 해당 노드에 할당하지 않음  
일반적으로 매니저 노드에 이 상태를 설정하지만 노드에 문제가 생겨 일시적으로 사용하지 않는 상태로 설정해야 할 때도 자주 사용됨  
```sh
$ docker node update \
  --availability drain \
  adwqqmreiawyyzar22iuw93wo
```
![](/docker_swarm/screenshots/docker_node_update_drain.png)
노드를 Drain 상태로 변경하면 해당 노드에서 실행중이던 서비스의 컨테이너는 모두 중지되고 Active 상태의 노드로 다시 할당됨  
그러나 Drain 상태의 노드를 Active 상태로 다시 변경한다고 해서 서비스의 컨테이너가 다시 분산되어 할당되지는 않으므로 `docker service scale` 명령어를 사용해 컨테이너의 균형을 재조정해야함  

#### **Pause**
Pause 상태는 서비스의 컨테이너를 더는 할당받지 않는다는 점에서는 Drain과 유사하나 실행중인 컨테이너가 중지되지는 않음  
노드의 상태를 Pause로 변경하려면 아래와 같이 입력  
```sh
$ docker node update \
  --availability pause \
  adwqqmreiawyyzar22iuw93wo
```
![](/docker_swarm/screenshots/docker_node_update_pause.png)

## 노드 라벨 추가  
노드에 라벨을 추가하는 것은 노드를 분류하는 것과 비슷함  
라벨은 키-값 형태를 가지고 있으며 키 값으로 노드를 구분할 수 있음  
노드에 라벨을 추가하면 서비스를 할당할 때 컨테이너를 생성할 노드의 그룹을 선택하는 ㄱ넛이 가능함  

### 노드에 라벨 추가  
노드 라벨 추가 명령어  
```sh
$ docker node update \
  --label-add storage=ssd \
  adwqqmreiawyyzar22iuw93wo
```
![](/docker_swarm/screenshots/docker_node_update_label_add.png)

### 서비스 제약 설정  
docker service create 명령어에 `--constraint` 옵션을 추가해 서비스의 컨테이너가 할당될 노드의 종류를 선택할 수 있음  
노드 라벨을 추가함으로써 제약조건을 설정할 수도 있지만 노드의 ID나 호스트 이름, 도커 데몬을 라벨 등으로도 제약 조건을 설정할 수 있음  

1. node.labels 제약조건  
    - storage=ssd로 설정된 노드에 서비스 컨테이너를 할당하는 명령어  
      ```sh
      $ docker service create --name label_test \
      --constraint 'node.labels.storage == ssd' \
      --replicas=5 \
      ubuntu:14.04 \
      ping docker.com
      ```
      ![](/docker_swarm/screenshots/docker_service_create_constraint.png)
    - `docker service ps` 명령어를 입력해 생성된 컨테이너를 확인해보면 storage 라벨이 ssd로 설정된 노드에만 컨테이너가 생성된 것을 확인할 수 있음   
      ![](/docker_swarm/screenshots/docker_service_ps_for_label.png)
    - 만약 해당 제한 조건에 해당되는 노드를 스웜 클러스터에서 찾지 못한다면 서비스의 컨테이너는 생성되지 않음  
2. node.id 제약조건  
  - node.id 조건에 노드의 ID를 명시하여 서비스의 컨테이너를 할당할 노드를 선택하는 방법  
  - 다른 도커 명령어와 달리 앞의 일부분만 입력하면 도커가 인식하지 못하므로 다음 예제와 같이 `docker node ls` 명령어에 출력된 ID를 전부 입력해야 함
    ```sh
    $ docker service create --name label_test2 \
      --constraint 'node.id == adwqqmreiawyyzar22iuw93wo' \
      --replicas=5 \
      ubuntu:14.04 \
      ping docker.com
    ```
    ![](/docker_swarm/screenshots/docker_service_create_node_id.png)
    ![](/docker_swarm/screenshots/docker_service_ps_test_label2.png)
3. node.hostname과 node.role 제약조건  
  - 스웜 클러스터에 등록된 호스트 이름 및 역할로 제한 조건을 설정  
    ```sh
    $ docker service create --name label_test3 \
      --constraint 'node.hostname == ip-172-31-91-255' \
      ubuntu:14.04 \
      ping docker.com
    ```
    ```sh
    $ docker service create --name label_test4 \
    --constraint 'node.role != manager' \
    ubuntu:14.04 \
    ping docker.com
    ```
4. engine.labels 제약조건  
  - 도커 엔진, 즉 도커 데몬 자체에 라벨을 설정해 제한조건을 설정  
  - 이를 사용하려면 도커 데몬의 실행옵션을 변경해야 함  
    ```sh
    $ DOCKER_OPTS="--label=mylabel=worker2 --label=mylabel2=second_worker"
    ```
  - 서비스를 생성할 때 engine.labels를 접두어로 제한조건을 설정하면 도커 데몬의 라벨을 사용할 수 있음  
  - 도커 데몬의 라벨 중 mylabel 이라는 키의 값이 worker2이고 mylabel2라는 키의  값이 second_worker로 설정된 노드에 서비스 컨테이너를 할당 (제한 조건은 여러개를 걸 수 있음)   
    ```sh
    $ docker service create --name label_test5 \
      --constraint 'engine.labels.mylabel == worker2' \
      --constraint 'engine.labels.mylabel2 == second_worker' \
      --replicas=3 \
      ubuntu:14.04 \
      ping docker.com
    ```