#!/bin/bash
# Create ES indices.
# Only for testing.
# In production they should be created by external service or somehow else.

# Create index using its name and json-tamplate with the index name
# param1 - index name
createIndexByName() {
  indexName=${1}
  curlResult=$(curl -X PUT "http://todo_test_server-kube-${CI_ENVIRONMENT_NAME}/${indexName}" -H 'Content-Type: application/json' \
  -d "@./gitlab-ci/${indexName}_index.json")

  resultOk="{\"acknowledged\":true,\"shards_acknowledged\":true,\"index\":\"${indexName}\"}"
  if [[ "${curlResult}" =~ "${resultOk}" ]]
  then
    printf "Index <%s> created.\n" "${indexName}"
  else
    printf "Error: %s\nIndex <%s> not created.\n" "${curlResult}" "${indexName}"
    exit 1
  fi
}

createIndexByName "order"
