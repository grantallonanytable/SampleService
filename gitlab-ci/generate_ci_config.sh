#!/bin/bash

# Набор шагов, для которых генерировать CI
stages="build;config;install;update;delete;del_ES_index;new_ES_index"
# Максимальное количество серверов (X) и стендов (XY)
maxX=8
maxY=8

generateNamespaces() {
# Генерировать конфигурации для стендов
maxX=${1}
maxY=${2}
stages=${3}
# Номера стендов (X)
for i in $(seq 1 $maxX)
do
# Номера стендов (Y)
    for j in $(seq 1 $maxY)
    do
        printf "
.namespace_test${i}${j}: &namespace_test${i}${j}_def
  environment:
    name: test${i}${j}
  variables:
    DOCKER_IMAGE_VERSION: $i$j-SNAPSHOT
    DB_URL: \"jdbc:oracle:thin:@//todo_test_server-db-${i}:${j}521/ORCLPDB1\"
    NON_PROXY_HOSTS: \"localhost|reactive-sandbox-test${i}${j}-reactive-sandbox-elasticsearch.test${i}${j}|*.todo_inner_net.ru\"
"
    done
done
}
# end generateNameSpaces

generateStages() {
# Генерировать шаги
maxX=${1}
maxY=${2}
stages=${3}
for i in $(seq 1 $maxX)
do
    for j in $(seq 1 $maxY)
    do
        for stage in $(echo $stages | tr ";" "\n")
        do
            stageLabel=$(printf "%s" $(tr '[:lower:]' '[:upper:]' <<< ${stage:0:1})${stage:1} | tr "_" " ")
            echo "
${stageLabel} [${i}${j}]:
  <<: *namespace_test${i}${j}_def
  <<: *${stage}_def"
        done
  done
done
}
# end generateStages

# begin script
cp .\\template.gitlab-ci.yml ..\\.gitlab-ci.yml
generateNamespaces $maxX $maxY $stages>> ..\\.gitlab-ci.yml
generateStages $maxX $maxY $stages >> ..\\.gitlab-ci.yml