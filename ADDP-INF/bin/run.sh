#!/bin/sh

pid=0

# SIGTERM-handler
term_handler() {
 if [ $pid -ne 0 ]; then
  kill -15 "$pid"
  wait "$pid"
 fi
 exit 143; # 128 + 15 -- SIGTERM
}
# setup handlers

trap 'kill ${!};term_handler' SIGTERM

# 等到jar包
jar=$(ls ./|grep \\.name)
env=$(ls ./|grep \\.env)
echo $jar
# 等到项目名
APP=$(echo ${jar%.name*})
env=$(echo ${env%.env*})
# 启动命令
echo "appName="$APP
echo "env="$env
if [ "$env" = "pro" ]
  then
      java -jar /home/admin/${APP}/${APP}.jar --spring.profiles.active=${env} &
  else
      # 非线上环境开启远程debug
      java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=9880 -jar /home/admin/${APP}/${APP}.jar --spring.profiles.active=${env} &
fi

pid="$!"

echo "pid="${pid}

while true
do
 tail -f /dev/null & wait ${!}
done
