#!/bin/bash

#Declare variables
WORKING_DIR="${HOME}/git/htmlcheck"
PROCESS_NAME="htmlcheck"

#Change directory
cd "${WORKING_DIR}"

#Pull last updates
git pull origin master

#Build
source "./local-jar.sh"

#Kill current process
pkill -f "${PROCESS_NAME}"

#Launch it again
source "./launch-production.sh"