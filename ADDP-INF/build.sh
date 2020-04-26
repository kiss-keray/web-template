#!/bin/sh
# 应用名
APP=$1
# 环境
ENV=$2
# 应用端口
port=$3
# 版本号
version=$4
# 寻找打包的jar包，移动到当前目录
#jar=$(find ../ -name ${APP}.jar)
cp /home/admin/${APP}/${APP}.jar /home/admin/${APP}/ADDP-INF/${APP}.jar
docker rm ${APP}-${ENV}
docker rmi ${APP}:${ENV}
if [ "$ENV" != "daily" ]
    then
      docker build -t ${APP}:${version} --build-arg APP_NAME=${APP} --build-arg ENV=${ENV} -f /home/admin/${APP}/ADDP-INF/Dockerfile /home/admin/${APP}/ADDP-INF
    else
      docker build -t ${APP}:daily --build-arg APP_NAME=${APP} --build-arg ENV=${ENV} -f /home/admin/${APP}/ADDP-INF/Dockerfile /home/admin/${APP}/ADDP-INF
fi
# docker 推送
