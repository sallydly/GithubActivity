#/bin/bash
# This shell is to swap from current activity to specified activity

ngSHA=$(docker ps | grep "ecs189_proxy_1" | awk -F' ' '{print $1}')

web1=$(docker ps | grep "web1" | wc -l)

if [[ web1 -gt 0 ]]
then
    docker run -d -P --network=ecs189_default --name=web2 $1
    docker exec -it $ngSHA /bin/bash swap2.sh
    web1SHA=$(docker ps | grep "web1" | awk -F' ' '{print $1}')
    docker rm -f $web1SHA
else
    docker run -d -P --network=ecs189_default --name=web1 $1
    docker exec -it $ngSHA /bin/bash swap1.sh
    web2SHA=$(docker ps | grep "web2" | awk -F' ' '{print $1}')
    docker rm -f $web2SHA
fi