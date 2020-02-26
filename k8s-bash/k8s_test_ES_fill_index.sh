#!/bin/bash
# Fill ES index with random data.
# Only for testing.
# In production it should be filled by external ds-order service.

# load file to string
# param1 - file name
loadJson() {
  echo $(cat ./k8s-bash/${1}|tr -d '\r\n\t ')
}

# add data record to ES index
# param1 - json data
# param2 - index name
# param3 - record id
generateRandomDataRecord() {
  jsonData=${1}
  indexName=${2}
  recordId=${3}
  printf ">>Insert order data: %s\n" "${jsonData}"
  # For curl execution ES must have ingress in k8s
  curlResult=$(curl -X PUT "http://reactive-sandbox-test-reactive-sandbox-elasticsearch/${indexName}/${indexName}/${id}" \
      -H "Content-Type: application/json" -d "$jsonData")
  resultOkCreated="\"created\":true"
  resultOkUpdated="\"result\":\"updated\"";
  if [[ ("${curlResult}" =~ "${resultOkCreated}") || ("${curlResult}" =~ "${resultOkUpdated}") ]]
  then
    printf ">>Record %s inserted.\n" "${id}"
    sleep .1
    return 0
  else
    printf ">>Record not inserted. Error: %s\n" "${curlResult}"
    sleep .1
    return 1
  fi
}

# fill fields in template
# param1 - json template
# param2 - index name
# param3 - id
prepareTemplate() {
  # no logging to stdin here!
  jsonTemplate=${1}
  indexName=${2}
  id=${3}
  # will be filled later (common fields) - 2 common parameterized fields
  # generate order id using namespaseId prefix
  clientEmail=$(printf "user%s@company.com" "${id}")
  clientPhone=$(printf "8-999-%03d-%02d-%02d"  $(($RANDOM % 1000))  $(($RANDOM % 100))  $(($RANDOM % 100)))
  creationDate=$(printf "2018-0%1s-1%1sT0%1s:00:00+03:00" $(((id / 100 %10) + 1)) $((id / 10 % 10)) $((id % 10)))
  # default values (common fields)
  clientName=""
  comment=""
  network=""
  parentNetwork=""
  partner=""
  region=""
  # returning value
  res=""
  if [ "$indexName" = "order" ]
  then
    # order
    # generated field data
    if [[ $(($RANDOM % 10)) > 6 ]]
    then
      attrFlag=$(printf "\"8-977-%03d-%02d-%02d\""  $(($RANDOM % 1000))  $(($RANDOM % 100))  $(($RANDOM % 100)))
    else
      attrFlag="null"
    fi
    shipGroupType=$((($RANDOM % 1000) + 1))
    totalPrice=$((($RANDOM / 100) + 1))
    shpi="${id}"
    # default values (order fields)
    cancelReason=""
    city=""
    comment="Comment to order"
    if [[ $(($RANDOM % 10)) > 6 ]]
    then
      orderCompositioSkuType=$(printf "TYPE %d"  $(($RANDOM % 1000)) )
    else
      orderCompositioSkuType="Тестовый СКУ"
    fi
    if [[ $(($RANDOM % 10)) > 6 ]]
    then
      orderCompositioCaption=$(printf "продукт с названием FRONT_%d"  $(($RANDOM % 1000)) )
    else
      orderCompositioCaption="тестовое название продукта из данного каталога"
    fi
    orderState="NEW"
    orderType=""
    payState=""
    payType=""
    promo=""
    salePointAddress=""
    salePointId=""
    printf -v res "${jsonTemplate}" "${cancelReason}" "${city}" "${clientEmail}" "${clientName}" "${clientPhone}" "${comment}" \
        "${creationDate}" "${id}" "${attrFlag}" "${network}" "${orderCompositioSkuType}" "${orderCompositioCaption}" "${orderState}" \
        "${orderType}" "${parentNetwork}" "${partner}" "${payState}" "${payType}" "${promo}" "${region}" "${salePointAddress}" \
        "${salePointId}" $shipGroupType $totalPrice "${shpi}"
  fi
  printf "${res}"
}

# add data records to specified ES index
# param1 - index name
generateRandomDataList() {
  indexName=${1}
  # extract namespace number if available
  namespaceId=$(( 1 * $(echo ${CI_ENVIRONMENT_NAME} | tr -cd '[[:digit:]]') ))
  printf "Generate random data (%s)\n" $indexName
  # read data template
  dataTemplate=$(loadJson $(printf "%s_data_template.json" $indexName))
  for cnt in $(seq 1 20)
  do
    id=$(printf "%d0%05d" $namespaceId $cnt)
    dataTemplatePrep=$(prepareTemplate "${dataTemplate}" "${indexName}" $id)
    generateRandomDataRecord "${dataTemplatePrep}" "${indexName}" $id
  done
}

generateRandomDataList "order"
