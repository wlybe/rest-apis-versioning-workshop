#! /bin/bash


access_token=`http --form post :8009/oauth2/token grant_type="client_credentials" client_id="customer2" client_secret="secret2" -p b | jq -r '.access_token'`

http :8080/isbns "Authorization: Bearer ${access_token}"
