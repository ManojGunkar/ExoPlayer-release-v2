#!/bin/sh

# if the deployment directory does not exist create it
if [ ! -d "$DEPLOY_DIR" ]; then
	mkdir -p "$DEPLOY_DIR"
fi

# if the firectory contains some files then move them to history folder
if [ "$(ls $DEPLOY_DIR)" ]; then
	if [ ! -d "${DEPLOY_SERVER_PATH}/History" ]; then
		mkdir -p "${DEPLOY_SERVER_PATH}/History"
	fi

	mv "${DEPLOY_DIR}"/Build* "${DEPLOY_SERVER_PATH}/History" 
fi

	
