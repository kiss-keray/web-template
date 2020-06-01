#!/bin/sh
# 应用名
APP=$1
# 环境
ENV=$2
# 应用端口
port=$3
# 应用端口设置端口
inPort=$4

logPath=$5

remontPort=$6

version=$7
echo "logPath=${logPath}"
# 启动应用镜像

docker stop ${APP}-${ENV}
docker rm ${APP}-${ENV}
if [ "$ENV" = "daily" ]
  then
     docker run  --name ${APP}-daily  -p ${port}:${inPort} -p ${remontPort}:9880 -v /tmp:/tmp -v ${logPath}:${logPath}  -dit ${APP}:daily
  else
    if [ "$ENV" = "pro" ]
      then
        docker run  --name ${APP}-${version}  -p ${port}:${inPort} -v /tmp:/tmp -v ${logPath}:${logPath}  -dit ${APP}:${version}
      else
        # 非线上环境开启远程debug
        docker run  --name ${APP}-${version}  -p ${port}:${inPort} -p ${remontPort}:9880 -v /tmp:/tmp -v ${logPath}:${logPath}  -dit ${APP}:${version}
    fi
fi
