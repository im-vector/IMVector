## docker build -t registry.cn-shenzhen.aliyuncs.com/imvector/imvectorserver -f Dockerfile .
## docker push  registry.cn-shenzhen.aliyuncs.com/imvector/imvectorserver

# 基础镜像, 需要的环境就是JDK
FROM registry.cn-shenzhen.aliyuncs.com/imvector/imvectorserver-base

# 使用root 操作
USER root

# 匿名卷，运行的时候可以使用 -v 参数挂载进来
# Spring Boot 使用的内嵌 Tomcat 容器默认使用/tmp作为工作目录
VOLUME /tmp

# 添加jar 文件到镜像中
ADD /docker/app.jar /usr/local/imvector/app.jar

ADD /docker/common /usr/local/imvector/lib

ENV PARAMS=""

# 运行jar 文件
ENTRYPOINT [ "sh", "-c", "java -jar $PARAMS -Dfile.encoding=UTF-8 /usr/local/imvector/app.jar" ]
