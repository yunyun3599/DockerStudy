```sh
$ mkdir certs

# self-signed root 인증서 (CA) 파일 생성
$ openssl genrsa -out ca.key 2048
$ openssl req -x509 -new -key ca.key -days 10000 -out ca.crt -subj "/"

# Root 인증서로 Registrt container에 사용될 인증서 생성
$ openssl genrsa -out domain.key 2048
$ openssl req -new -key domain.key -subj /CN=${DOCKER_HOST_IP} -out domain.csr # DOCKER_HOST_IP는 레지스트리 컨테이너가 존재하는 도커 호스트 서버의 IP or 도메인 이름
$ echo subjectAltName = IP:${DOCKER_HOST_IP} > extfile.cnf
$ openssl x509 -req -in domain.csr -CA ca.crt -CAkey ca.key -CAcreateserial -out domain.crt -days 10000 -extfile extfile.cnf

# 레지스트리 로그인 시에 사용할 계정과 비밀번호 저장 파일
$ htpasswd -c htpasswd jenn.y
New password: jenn.y
Re-type new password jenn.y
Adding password for user jenn.y

# 현재 디렉토리에 nginx.conf 파일 작성

# registry 컨테이너 생성
$ docker run -d --name myregistry --restart=always registry:2.6

# nginx 컨테이너 생성
$ docker run -d --name nginx_frontend \
  -p 443:443 \
  --link myregistry:registry \
  -v $(pwd)/:/etc/nginx/conf.d \
  nginx:1.9

# 로그인 
$ docker login https://${DOCKER_HOST_IP}
# Error response from daemon: Get "https://192.168.35.96/v2/": dialing 192.168.35.96:443 no HTTPS proxy: connecting to 192.168.35.96:443: dial tcp 192.168.35.96:443: connect: connection refused

# ca.crt 파일 신뢰할 수 있는 인증서 목록에 추가
$ sudo security add-trusted-cert -d -r trustRoot -k "/Library/Keychains/System.keychain" "[경로/root인증서파일명]"
```
