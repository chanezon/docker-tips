#!/bin/bash
set -x

echo "starting ucp cluster configuration"
date
ps ax

SWARM_VERSION="swarm:1.1.0"
#############
# Parameters
#############

MASTERCOUNT=${1}
MASTERPREFIX=${2}
MASTERFIRSTADDR=${3}
AZUREUSER=${4}
MASTERFQDN=${5}
POSTINSTALLSCRIPTURI=${6}
VMNAME=`hostname`
VMNUMBER=`echo $VMNAME | sed 's/.*[^0-9]\([0-9]\+\)*$/\1/'`
VMPREFIX=`echo $VMNAME | sed 's/\(.*[^0-9]\)*[0-9]\+$/\1/'`
BASESUBNET="172.16.0."

echo "Master Count: $MASTERCOUNT"
echo "Master Prefix: $MASTERPREFIX"
echo "Master First Addr: $MASTERFIRSTADDR"
echo "vmname: $VMNAME"
echo "VMNUMBER: $VMNUMBER, VMPREFIX: $VMPREFIX"
echo "BASESUBNET: $BASESUBNET"
echo "AZUREUSER: $AZUREUSER"

###################
# Common Functions
###################

ensureAzureNetwork()
{
  # ensure the host name is resolvable
  hostResolveHealthy=1
  for i in {1..120}; do
    host $VMNAME
    if [ $? -eq 0 ]
    then
      # hostname has been found continue
      hostResolveHealthy=0
      echo "the host name resolves"
      break
    fi
    sleep 1
  done
  if [ $hostResolveHealthy -ne 0 ]
  then
    echo "host name does not resolve, aborting install"
    exit 1
  fi

  # ensure the network works
  networkHealthy=1
  for i in {1..12}; do
    wget -O/dev/null http://bing.com
    if [ $? -eq 0 ]
    then
      # hostname has been found continue
      networkHealthy=0
      echo "the network is healthy"
      break
    fi
    sleep 10
  done
  if [ $networkHealthy -ne 0 ]
  then
    echo "the network is not healthy, aborting install"
    ifconfig
    ip a
    exit 2
  fi
  # ensure the host ip can resolve
  networkHealthy=1
  for i in {1..120}; do
    hostname -i
    if [ $? -eq 0 ]
    then
      # hostname has been found continue
      networkHealthy=0
      echo "the network is healthy"
      break
    fi
    sleep 1
  done
  if [ $networkHealthy -ne 0 ]
  then
    echo "the network is not healthy, cannot resolve ip address, aborting install"
    ifconfig
    ip a
    exit 2
  fi
}
ensureAzureNetwork
HOSTADDR=`hostname -i`

ismaster ()
{
  if [ "$MASTERPREFIX" == "$VMPREFIX" ]
  then
    if [ "$VMNUMBER" == '0' ]
    then
      return 0
    else
      return 1
    fi
  else
    return 1
  fi
}
if ismaster ; then
  echo "this node is a master"
fi

isreplica ()
{
  if [ "$MASTERPREFIX" == "$VMPREFIX" ]
  then
    if [ "$VMNUMBER" == '0' ]
    then
      return 1
    else
      return 0
    fi
  else
    return 1
  fi
}
if isreplica ; then
  echo "this node is a replica"
fi

isagent()
{
  if ismaster ; then
    return 1
  else
    if isreplica ; then
        return 1
    else
        return 0
    fi
  fi
}
if isagent ; then
  echo "this node is an agent"
fi

isagentorreplica()
{
  if ismaster ; then
    return 1
  else
    return 0
  fi
}

######################
# resolve self in DNS
######################

echo "$HOSTADDR $VMNAME" | sudo tee -a /etc/hosts

################
# Install Docker
################

echo "Installing and configuring docker"

installDocker()
{
  curl -s 'https://sks-keyservers.net/pks/lookup?op=get&search=0xee6d536cf7dc86e2d7d56f59a178ac6c6238f52e' | sudo apt-key add --import
  sudo apt-get update && sudo apt-get install -y apt-transport-https
  sudo apt-get install -y linux-image-extra-virtual
  echo "deb https://packages.docker.com/1.10/apt/repo ubuntu-trusty main" | sudo tee /etc/apt/sources.list.d/docker.list
  sudo apt-get update && sudo apt-get install -y docker-engine
  if [ $? -eq 0 ]
  then
    # hostname has been found continue
    echo "Docker installed successfully"
    break
  fi
}
time installDocker
#sudo usermod -aG docker $AZUREUSER
if isagent ; then
  # Start Docker and listen on :2375 (no auth, but in vnet)
  echo 'DOCKER_OPTS="-H unix:///var/run/docker.sock -H 0.0.0.0:2375"' | sudo tee -a /etc/default/docker
fi

sudo service docker restart

ensureDocker()
{
  # ensure that docker is healthy
  dockerHealthy=1
  for i in {1..3}; do
    sudo docker info
    if [ $? -eq 0 ]
    then
      # hostname has been found continue
      dockerHealthy=0
      echo "Docker is healthy"
      sudo docker ps -a
      break
    fi
    sleep 10
  done
  if [ $dockerHealthy -ne 0 ]
  then
    echo "Docker is not healthy"
  fi
}
ensureDocker

MASTEROCTET=`expr $MASTERFIRSTADDR + $VMNUMBER`
VMIPADDR="${BASESUBNET}${MASTEROCTET}"
MASTER0IPADDR="${BASESUBNET}${MASTERFIRSTADDR}"

if ismaster ; then
  sudo docker run --rm -i \
    --name ucp \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v /opt/azure/containers/docker_subscription.lic:/docker_subscription.lic \
    docker/ucp \
    install --san $MASTERFQDN --fresh-install
  echo "completed starting UCP on the master"
fi

if isagentorreplica ; then
  REPLICAPARAM=""
  if isreplica ; then
    REPLICAPARAM="--replica"
  fi
  FPRINT=`echo | openssl s_client -connect ${MASTER0IPADDR}:443 |& openssl x509 -fingerprint -noout | cut -f2 -d'='`
  echo $FPRINT
  sudo docker run --rm -i \
    --name ucp \
    -v /var/run/docker.sock:/var/run/docker.sock \
    -v /opt/azure/containers/docker_subscription.lic:/docker_subscription.lic \
    -e UCP_ADMIN_USER=admin \
    -e UCP_ADMIN_PASSWORD=orca \
    docker/ucp \
    join $REPLICAPARAM --san $MASTERFQDN --fresh-install --url https://${MASTER0IPADDR}:443 --fingerprint "${FPRINT}"
  echo "completed starting UCP on the agent or replica"
fi

if [ $POSTINSTALLSCRIPTURI != "disabled" ]
then
  echo "downloading, and kicking off post install script"
  /bin/bash -c "wget --tries 20 --retry-connrefused --waitretry=15 -qO- $POSTINSTALLSCRIPTURI | nohup /bin/bash >> /var/log/azure/cluster-bootstrap-postinstall.log 2>&1 &"
fi

echo "processes at end of script"
ps ax
date
echo "completed ucp cluster configuration"

echo "restart system to install any remaining software"
if isagent ; then
  shutdown -r now
else
  # wait 1 minute to restart master
  /bin/bash -c "shutdown -r 1 &"
fi