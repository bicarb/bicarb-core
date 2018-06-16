#!/usr/bin/env bash

# WARNING: This script only work for ubuntu 16.04
# It is not a good script, you should not trust it. :(
command -v apt > /dev/null 2>&1 || { echo >&2 "No command 'apt' found."; exit 1; }

if [[ -z "$1" ]]; then
  echo "Please specify a database password, ./install.sh databasePassword";
  exit 1;
else
  pw="$1";
fi

workDir="/usr/local/bicarb"
user="bicarb"

if ! id -u ${user} > /dev/null 2>&1; then
  adduser --home ${workDir} --shell /bin/false --no-create-home --disabled-password --disabled-login bicarb
fi

if [[ ! -d "${workDir}" ]]; then
  mkdir ${workDir}
  chown -R bicarb:bicarb ${workDir}
fi

function initJdk() {
  if type -p java; then
    echo "[jdk][skip]found jdk executable in PATH, please insure jdk version >= 10"
    _java=java
  elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
    echo "[jdk][skip]found jdk executable in JAVA_HOME, please insure jdk version >= 10"
    _java="$JAVA_HOME/bin/java"
  else
    echo "[jdk]install jdk 10"
    sudo add-apt-repository ppa:linuxuprising/java
    sudo apt update
    sudo apt install oracle-java10-installer
    sudo apt install oracle-java10-set-default
  fi
}

function initPostgreSQL() {
  if type -p psql; then
    echo "[pg][skip]found PostgreSQL executable in PATH"
  else
    echo "[pg]install PostgreSQL"
    echo "deb http://apt.postgresql.org/pub/repos/apt/ xenial-pgdg main" > /etc/apt/sources.list.d/pgdg.list
    wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | sudo apt-key add -
    sudo apt update
    sudo apt install postgresql-10
  fi

  echo "[pg]init default account and database"
  cat > /tmp/pg.sh << "EOF"
#!/usr/bin/env bash

pw=$1

echo "[pg]Change to postgres to init database. Your database password is: '$pw'"
id

# init user
psql -tAc "SELECT 1 FROM pg_roles WHERE rolname='bicarb'" | grep -q 1 &&
  echo "[pg][skip]User 'bicarb' already exists." ||
  psql -c "CREATE ROLE bicarb WITH LOGIN CREATEDB PASSWORD '$pw'"

# init db
psql -tAc "SELECT 1 FROM pg_database WHERE datname='bicarb'" | grep -q 1 &&
  echo "[pg][skip]Database 'bicarb' already exists" ||
  psql -c "CREATE DATABASE bicarb OWNER bicarb"

# if [ "$( psql -tAc "SELECT 1 FROM pg_database WHERE datname='bicarb'" )" == '1' ]; then
#   echo "[pg][skip]Database 'bicarb' already exists"
# else
#   psql -c "CREATE DATABASE bicarb OWNER bicarb"
# fi
EOF
  chmod 755 /tmp/pg.sh
  cd /tmp
  sudo -u postgres bash -c "/tmp/pg.sh \"${pw}\""
  rm /tmp/pg.sh
}

function initBicarb() {
  type -p git || apt install git

  cd ${workDir}
  if [[ -d "bicarb-core" ]]; then
    rm -R bicarb-core
  fi

  git clone https://github.com/bicarb/bicarb-core.git --depth=1
  cd bicarb-core
  chmod 744 ./mvnw
  ./mvnw install -DskipTests=true -Dmaven.javadoc.skip=true -B -V
  cp ./target/bicarb-core-*-SNAPSHOT.jar ../bicarb-core.jar
  cp -R ./templates ../templates

  cd ${workDir}
}

function initConfigFile() {
  cd ${workDir}
  if [[ -e ./application-production.yml ]]; then
    echo "[config][skip]Config file already exists"
  else
    echo "[config]generate a config file"
    cat > application-production.yml << EOF
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bicarb
    username: bicarb
    password: ${pw}

EOF
  fi
}

function initNginx() {
  if type -p nginx; then
    echo "[nginx][skip]nginx already exists"
  else
    echo "[nginx]install nginx"
    wget https://nginx.org/keys/nginx_signing.key
    sudo apt-key add nginx_signing.key
    echo "deb http://nginx.org/packages/ubuntu/ xenial nginx" >> /etc/apt/sources.list
    echo "deb-src http://nginx.org/packages/ubuntu/ xenial nginx" >> /etc/apt/sources.list
    sudo apt update
    sudo apt install nginx
    cat > /etc/nginx/nginx.conf << EOF
user  ${user} ${user};
worker_processes  1;

error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;

events {
    multi_accept on;
    worker_connections  1024;
}

http {
    include       /etc/nginx/mime.types;
    default_type  application/octet-stream;

    log_format  main  '\$remote_addr - \$remote_user [\$time_local] "\$request" '
                      '\$status \$body_bytes_sent "\$http_referer" '
                      '"\$http_user_agent" "\$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    charset         utf-8;
    server_tokens   off;

    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;

    reset_timedout_connection on;
    send_timeout 10;

    keepalive_timeout  65;

    gzip  on;
    gzip_vary          on;
    gzip_comp_level    5;
    gzip_buffers       16 8k;
    gzip_min_length    1024;
    gzip_proxied       any;
    gzip_disable       "msie6";
    gzip_http_version  1.0;
    gzip_types         text/plain text/css application/json application/vnd.api+json application/x-javascript text/xml application/xml application/xml+rss text/javascript application/javascript image/svg+xml;

    include /etc/nginx/conf.d/*.conf;
}

EOF
    cat > /etc/nginx/conf.d/default.conf << "EOF"
upstream bicarb {
    server 127.0.0.1:8080;
}

server {
    listen       80 fastopen=3 reuseport;
    server_name  _; # your domain

    location / {
        proxy_hide_header   X-Powered-By;
        proxy_set_header    X-Real-IP $remote_addr;
        proxy_set_header    Host      $http_host;
        proxy_set_header    X-Forwarded-For    $proxy_add_x_forwarded_for;
        proxy_set_header    X-Forwarded-Proto  $scheme;
        proxy_pass  http://bicarb;
    }

    error_page   500 502 503 504  /50x.html;
    location = /50x.html {
        root   /usr/share/nginx/html;
    }
}

EOF
  fi
}

function initSystemd() {
  if [[ -e /etc/systemd/system/bicarb.service ]]; then
    echo "[systemd][skip]bicarb service already exists"
  else
    echo "[systemd]generate '/etc/systemd/system/bicarb.service'"
    cat > /etc/systemd/system/bicarb.service << EOF
[Unit]
Description=Bicarb Core Application
After=syslog.target

[Service]
User=${user}
Group=${user}
WorkingDirectory=${workDir}
ExecStart=/usr/bin/java -server -jar bicarb-core.jar --spring.profiles.active=production
SuccessExitStatus=143

RestartSec=30
Restart=always

[Install]
WantedBy=multi-user.target

EOF
    echo "[systemd]systemctl enable bicarb.service"
    systemctl enable bicarb.service
  fi
}

function initIptables() {
  if [[ $(dpkg-query -W -f='${Status}' iptables-persistent 2>/dev/null | grep -c "ok installed") == '0' ]]; then
    apt install iptables-persistent
    iptables -t filter -A INPUT -p tcp -m tcp --dport 5432 -s localhost -j ACCEPT
    iptables -t filter -A INPUT -p tcp -m tcp --dport 5432 -j REJECT
    ip6tables -t filter -A INPUT -p tcp -m tcp --dport 5432 -s localhost -j ACCEPT
    ip6tables -t filter -A INPUT -p tcp -m tcp --dport 5432 -j REJECT
    iptables -t filter -A INPUT -p tcp -m tcp --dport 8080 -s localhost -j ACCEPT
    iptables -t filter -A INPUT -p tcp -m tcp --dport 8080 -j REJECT
    ip6tables -t filter -A INPUT -p tcp -m tcp --dport 8080 -s localhost -j ACCEPT
    ip6tables -t filter -A INPUT -p tcp -m tcp --dport 8080 -j REJECT
    iptables-save > /etc/iptables/rules.v4
    ip6tables-save > /etc/iptables/rules.v6
  else
    echo "[iptables][skip]'iptables-persistent' is already exists"
  fi
}

# start init
initJdk
initPostgreSQL
initBicarb
initConfigFile
initNginx
initSystemd
initIptables
