#! /bin/bash

# 本地编译
docker run --rm -u root -v "$PWD":/home/gradle/project \
-v /Users/vector.huang/.m2/repository:/root/.m2/repository \
-v /Users/vector.huang/.gradle:/home/gradle/.gradle \
-w /home/gradle/project gradle:5.4-jdk11 gradle cleanLibs

docker run --rm -u root -v "$PWD":/home/gradle/project \
-v /Users/vector.huang/.m2/repository:/root/.m2/repository \
-v /Users/vector.huang/.gradle:/home/gradle/.gradle \
-w /home/gradle/project gradle:5.4-jdk11 gradle copyJars

docker run --rm -u root -v "$PWD":/home/gradle/project \
-v /Users/vector.huang/.m2/repository:/root/.m2/repository \
-v /Users/vector.huang/.gradle:/home/gradle/.gradle \
-w /home/gradle/project gradle:5.4-jdk11 gradle mvCommonJar

docker run --rm -u root -v "$PWD":/home/gradle/project \
-v /Users/vector.huang/.m2/repository:/root/.m2/repository \
-v /Users/vector.huang/.gradle:/home/gradle/.gradle \
-w /home/gradle/project gradle:5.4-jdk11 gradle bootJar

# 打包成镜像
docker build -t registry.cn-shenzhen.aliyuncs.com/imvector/imvectorserver .
docker push registry.cn-shenzhen.aliyuncs.com/imvector/imvectorserver
