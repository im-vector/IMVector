#! /bin/bash

# /root/.gradle -> /home/gradle/.gradle
# 用root 用户把，不然可能出现权限不够
# root 对应的 .m2 为：/root/.m2/repository
docker run --rm -u root -v "$PWD":/home/gradle/project \
-v /Users/vector.huang/.m2/repository:/root/.m2/repository \
-v /Users/vector.huang/.gradle:/home/gradle/.gradle \
-w /home/gradle/project gradle:5.4-jdk11 gradle publishToMavenLocal
