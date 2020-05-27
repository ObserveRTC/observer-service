#!/bin/bash

# install typescript to json generator
npm install --save ts-json-schema-generator
# npm list typescript-to-json-schema || npm install typescript-to-json-schema

temp_directory="downloaded"
target_directory="../main/resources/webrtcstatschemas"

if [[ "$1" != "--usecache" ]]; then
  read -p 'Provide the URL of the sender_payloads: ' sender_payloads
  curl -# "$sender_payloads" --output "$temp_directory/sender_payloads.ts"
fi

for type in MediaSource \
  CandidatePair \
  RemoteCandidate \
  LocalCandidate \
  Track \
  OutboundRTP \
  InboundRTP \
  RemoteInboundRTP \
  StatsPayload; do
  # source /node_modules/.bin/typescript-to-json-schema \

  ./node_modules/.bin/ts-json-schema-generator \
    --path "$temp_directory/sender_payloads.ts" \
    --type="$type" \
    --expose 'export' \
    --jsDoc 'extended' >"$target_directory/$type.json"
done
