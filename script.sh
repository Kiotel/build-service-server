LOCK_FILE="$(pwd)/myLock.lock"
flock -n $LOCK_FILE ./update.sh >> ./deploy.log 2>&1