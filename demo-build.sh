#!/bin/sh

ACESS_TOKEN=GQvEQGCs1TCnbEd_z9DG


curl -k -X POST \
    -F token=ddfbe49f46166c4a0fcd7d6d93cda3 \
    -F ref=release/demo \
    -F variables[BUILD_FLAVOR]=demo \
    https://gitlab.globaldelight.com/api/v3/projects/48/trigger/builds

 #TODO: 
 # 1. Monitor build progess
 # 2. Download the apk
 # 3. Upload to dropbox
 # 4. Generate the Share URL