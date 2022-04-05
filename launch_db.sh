#!/usr/bin/env bash
# Creates, if necessary, and starts a MongoDB Docker container.

NAME=$1
PORT=$2

# List containers with vrem-container in the name.
containers=$(docker ps -al | grep "$NAME")

if [ -z "$containers" ]; then
  # Variable empty: Run new container.
  echo "No container named $NAME found, creating new one."
  docker run --name "$NAME" -d -p "$PORT":27017 mongo:4.4.8-focal
  echo "Started new container."

else
  # Container exists.
  if [[ $containers == *Exited* ]]; then
    # Stopped, restart.
    echo "Existing container was stopped. Restarting."
    docker start "$NAME"
    echo "Restarted container."

  else
    # Already running, do nothing.
    echo "$NAME already running."

  fi

fi
