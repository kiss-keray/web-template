#!/bin/sh

helmNamespace='50214-keray'

dockerUrl='docker.keray.com'
staticChartVersion='1.1.0'
# a APP名
# d dockerfile路径
# c chartPath路径
# v 发布版本key
# s 静态镜像版本
# i install helm
# n 检查空间
# t chart静态版本
while getopts ":a:j:d:c:v:s:i:n:t:" opt; do
  case $opt in
  a)
    APP=$OPTARG
    ;;
  j)
    jar_name=$OPTARG
    ;;
  d)
    dockerfile=$OPTARG
    ;;
  c)
    chartPath=$OPTARG
    ;;
  v)
    versionKey=$OPTARG
    ;;
  s)
    staticVersion=$OPTARG
    ;;
  i)
    init=$OPTARG
    ;;
  n)
    namespace=$OPTARG
    ;;
  t)
    staticChartVersion=$OPTARG
    ;;
  ?)
    echo "未知参数"
    ;;
  esac
done
echo "脚本参数 APP=${APP} jar_name=${jar_name} dockerfile=${dockerfile} chartPath=${chartPath} versionKey=${versionKey}
staticVersion=${staticVersion} init=${init} namespace=${namespace} staticChartVersion=${staticChartVersion}"
# cd到应用目录
#cd /home/admin/${APP}

#版本号写入Chart.yaml
writeVersionHelm() {
  sed -i "s#1\.0\.0#${chartVersion}#" ${chartPath}/Chart.yaml
  sed -i "s#_appVersion#${imageVersion}#" ${chartPath}/Chart.yaml
}

#自动生成版本号
autoVersion() {
  echo "开始版本设置"
  if [ "$staticVersion" ]; then
    # 静态镜像版本时只升级chart版本，不升级镜像版本 节约镜像空间
    imageVersion=$staticVersion
    chartVersion=$staticChartVersion
    echo "（静态）版本设置成功.version="${imageVersion}
    writeVersionHelm
  else
    #  获取版本
    result=$(wget https://admin.cswx.com/v3/sys/config/system-version/get?key=${versionKey} --no-check-certificate -O version 2>&1)
    _200=$(echo $result | grep 200)
    _404=$(echo $result | grep 404)
    if [ "$_200" ]; then
      # 设置chart版本
      chartVersion=$(cat version)
      # 镜像动态版本设置
      imageVersion=$(cat version)
      echo "版本设置成功.version="${imageVersion}
      #更新版本
      result=$(wget https://admin.cswx.com/v3/sys/config/system-version/update?key=${versionKey} --no-check-certificate -O version 2>&1)
      _200=$(echo $result | grep 200)
      if [ "$_200" ]; then
        echo "版本更新成功"
      else
        echo "版本更新失败"
        exit 1
      fi
      writeVersionHelm
    else
      if [ "$_404" ]; then
        # 520时直接设置1.0.0版本
        imageVersion="1.0.0"
        chartVersion="1.0.0"
        echo "获取版本404,设置version="${imageVersion}
      else
        echo "获取版本失败!!!"
        exit 1
      fi
      writeVersionHelm
    fi
  fi
}
# 镜像部分
dockerWork() {
  echo "sh:cp /home/admin/${APP}/${jar_name}.jar /home/admin/${APP}/ADDP-INF/${jar_name}.jar"
  cp /home/admin/${APP}/${jar_name}.jar /home/admin/${APP}/ADDP-INF/${jar_name}.jar
  #build
  echo "sh:build -t ${dockerUrl}/${APP}:${imageVersion} -f ${dockerfile} /home/admin/${APP}/ADDP-INF"
  docker build -t ${dockerUrl}/${APP}:${imageVersion} -f ${dockerfile} /home/admin/${APP}/ADDP-INF
  # docker 推送
  echo "sh:docker push ${dockerUrl}/${APP}:${imageVersion}"
  docker push ${dockerUrl}/${APP}:${imageVersion}
}
#部署部分
deploy() {
  # 推送chart
  echo "sh:helm push ${chartPath} $helmNamespace"
  helm push ${chartPath} $helmNamespace

  helm repo update
  echo "chart 版本="${chartVersion}
  if [ "$init" ]; then
    echo "初始化release"
    echo "sh:helm install --set version=${chartVersion} ${APP} ${helmNamespace}/${APP}"
    helm install --set version=${chartVersion} ${APP} ${helmNamespace}/${APP}
  else
    echo "升级release"
    echo "sh:helm upgrade --set version=${chartVersion} ${APP} ${helmNamespace}/${APP}"
    helm upgrade --set version=${chartVersion} ${APP} ${helmNamespace}/${APP}
  fi
}
health_check() {
  sleep 10
  echo "checking "
  while true; do
    status=$(kubectl get pods --namespace ${namespace} | grep ${APP} | grep 0/1)
    if [ "$status" ]; then
      sleep 1
      ((exptime++))
      echo -e "\rWait app to pass health check: $exptime..."
      if [ $exptime -gt 600 ]; then
        echo 'app start failed'
        exit 1
      fi
    else
      break
    fi
  done
  echo "check success"
}

autoVersion
dockerWork
deploy
health_check
