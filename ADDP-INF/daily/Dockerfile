# 拉取基础镜像
FROM registry.cn-shanghai.aliyuncs.com/base-service/base-image:1.0.0

# 指定运行时的系统环境变量,如下请替换appName为自己应用名称


# 创建文件夹
RUN mkdir -p  /home/admin/web-project/
RUN mkdir -p  /home/admin/bin/

# 将应用启动脚本配置复制到镜像中
COPY bin/run.sh /home/admin/bin

# 复制应用到镜像
COPY web-project.jar /home/admin/web-project/web-project.jar

RUN echo 'hello word'> /home/admin/bin/web-project.name

RUN echo 'hello word'> /home/admin/bin/daily.env

# 设置文件夹操作权限
RUN chmod -R a+x /home/admin/bin/run.sh
#RUN chown -R admin:admin /home/admin/$APP_NAME/

# 启动容器时进入的工作目录
WORKDIR /home/admin/bin/

#容器启动时自动执行的脚本，我们一般会将应用启动脚本放在这里，相当于系统自启应用
ENTRYPOINT ["./run.sh"]
