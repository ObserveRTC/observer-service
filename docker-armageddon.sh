read -p "Are you sure? " -n 1 -r

if [[ $REPLY =~ ^[Nn]$ ]]; then
  echo "Okay! Cool your jets down, drink a tea, and figure out why you wanna destroy your docker realm."
  exit
fi

if [[ $REPLY =~ ^[Yy]$ ]]; then
  echo "\n\n Docker Armageddon has been launched, all container will be purged.\n\n"
  docker stop $(docker ps -aq)
  docker rm $(docker ps -aq)
  docker network prune -f
  docker rmi -f $(docker images --filter dangling=true -qa)
  docker volume rm $(docker volume ls --filter dangling=true -q)
  docker rmi -f $(docker images -qa)
fi
