#!/bin/sh

if [ -d "$DEPLOY_DIR" ]; then
	mkdir -p "${DEPLOY_SERVER_PATH}/History"
	mv "${DEPLOY_DIR}/Build_* ${DEPLOY_SERVER_PATH}/History/" 
else
	mkdir -p "$DEPLOY_DIR"
fi
