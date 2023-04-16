**워커노드 컨테이너 생성??**

### ec2
ec2 실행  

### shell 접속
```sh
# 컨테이너 접속
$ sudo apt update

# 필요 패키지 설치
$ sudo apt install apt-transport-https ca-certificates curl gnupg-agent software-properties-common

# 리포지토리 GPG 키 가져오기 & 도커 APT 리포지토리 시스템에 추가
$ sudo mkdir -p /etc/apt/keyrings
$ curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
$ echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 도커 설치
$ sudo apt update
$ sudo apt install docker-ce docker-ce-cli containerd.io

# 도커 그룹에 user 추가
$ sudo usermod -aG docker $USER
# 권한 변경
$ sudo chmod 666 /var/run/docker.sock
# 도커 서비스 재시작
$ sudo service docker restart
```