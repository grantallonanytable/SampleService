#!/bin/bash
# Delete ES index.
# Only for testing.
# Use only if it strongly required.

# delete specified index
# param 1 - index name
deleteIndex() {
  indexName=$1
    curlResult=$(curl -X DELETE "http://reactive-sandbox-test-reactive-sandbox-elasticsearch/${indexName}")

    resultOk="{\"acknowledged\":true}"
    if [[ "${curlResult}" =~ "${resultOk}" ]]
    then
      printf "Index <%s> deleted.\n" "${indexName}"
    else
      printf "Error: %s\nIndex <%s> not deleted.\n" "${curlResult}" "${indexName}"
      exit 1
    fi
}

deleteIndex "order"

