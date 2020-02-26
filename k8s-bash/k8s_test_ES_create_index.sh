#!/bin/bash
# Create ES indices.
# Only for testing.
# In production they should be created by external service or somehow else.

# Create index using its name and json-tamplate with the index name
# param1 - index name
createIndexByName() {
  indexName=${1}
  curlResult=$(curl -X PUT "http://reactive-sandbox-test-reactive-sandbox-elasticsearch/${indexName}" -H 'Content-Type: application/json' \
  -d "@./k8s-bash/${indexName}_index.json")

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
