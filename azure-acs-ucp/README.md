# Azure Resource Manager Template to provision a Docker Universal Control Plane cluster

## Introduction

This is part of the [P@ Docker Tips](/README.md) series or tips about Docker.

For [#SwarmWeek](https://twitter.com/search?q=%23SwarmWeek) here is an [Azure resource Manager](https://azure.microsoft.com/en-us/documentation/articles/resource-group-overview/) template as a basis to provision a cluster running [Docker Universal Control Plane](https://docs.docker.com/ucp/overview/) on Microsoft Azure. 

This template is evolved from Microsoft's [Azure Container Service](https://azure.microsoft.com/en-us/documentation/services/container-service/) base template to provision Swarm cluster. ACS comes in 2 flavors, Mesos and Swarm. I took the [Swarm template]((https://github.com/Azure/azure-quickstart-templates/tree/master/101-acs-swarm)) as a base. More specifically the [ACS Swarm template](https://github.com/Azure/azure-quickstart-templates/blob/master/101-acs-swarm/azuredeploy.json) takes a series of high level parameters like number of masters and agents and provisions a `"Microsoft.ContainerService/containerServices"` service with it. This service is actually implemented behind the scenes by a more complex template who actually provisions the infrastructure, and is similar to [anhowe's swarm-cluster-with-no-jumpbox.json](https://github.com/anhowe/scratch/blob/master/mesos-marathon-vmss/swarm-cluster-with-no-jumpbox.json). This is the template I used as a base, trying to minimize modifications to be able to rebase when the master template changes.

I modified this template to provision a Docker Universal Control Plane cluster.

This template provisions:
* one network with 2 subnet, master and agents
* n VMs for masters in an availability set: I install Docker CS and UCP master in the first VM, and n-1 replicas in the other VMs
* master load balancer on port 443 for the UCP UI, and NAT rules to ssh into each box at ports 2200 and above 
* p VMs for agents in a VM scale set: I install Docker CS and UCP nodes joining the master on these 
* agent load balancer on ports 443, 80 and 8080, with NAT rules to ssh from ports 50000 and above

## Installation

Deploy to Azure directly from there:

<a href="https://portal.azure.com/#create/Microsoft.Template/uri/https%3A%2F%2Fraw.githubusercontent.com%2Fchanezon%2Fdocker-tips%2Fmaster%2Fazure-acs-ucp%2Fazuredeploy.json" target="_blank">
    <img src="http://azuredeploy.net/deploybutton.png"/>
</a>
<a href="http://armviz.io/#/?load=https%3A%2F%2Fraw.githubusercontent.com%2Fchanezon%2Fdocker-tips%2Fmaster%2Fazure-acs-ucp%2Fazuredeploy.json" target="_blank">
    <img src="http://armviz.io/visualizebutton.png"/>
</a>

Or use the Azure CLI and follow these steps:
* Install azure cli
* azure login
* get a [trial or production ucp license](https://docs.docker.com/ucp/production-install/#step-7-license-your-installation)
* edit azuredeploy.parameters.json to parametrize your cluster
  * masterEndpointDNSNamePrefix and agentEndpointDNSNamePrefix will be public dns names in Azure where you cluster is exposed. The DNS names will be `[masterEndpointDNSNamePrefix].[azure-location].cloudapp.azure.com`
  * ucpInstallScriptContent lets you customize the UCP installation script if you need to. Else the default should work.
  * dockerLicenceData paste the result of cat docker_subscription.lic|gzip|base64
  * sshRSAPublicKey paste your ssh public key to be able to ssh in the machines
  * you can modify masterCount and agentCount as well as agentVMSize
* create your group and deploy the template with your own values for the resource group name and deployment

```  
azure group create -n "patucp"  -l "West US"
azure group deployment create "patucp" "patucpdep"  -f azuredeploy.json -e azuredeploy.parameters.json
```

15 minutes later you should have a working UCP cluster with 3 masters and 1 node. Nodes are in a VM Scale Set so you should be able to add a rule to autoscale that.

The template outputs the fully qualified domain name of the master and agent pools. In your browser, open it on https port: `https://[masterFQDN]`. Ignore Chrosme's warning and say you want to proceed anyway. You should end up in UCP login screen. Default credentials are admin/orca. Don't forget to change them as soon as you are logged in.

<img src="/img/ucp.png"/>

Then you can [setup certificates for the docker cli](https://docs.docker.com/ucp/production-install/#step-10-set-up-certificates-for-the-docker-cli) and start launching containers in your new cluster.

Have fun with UCP!

## Notes

This is a work in progress. Here are a few issues and limitations with the current template that I need to fix (PRs welcome:-):
* bug: right now Docker CS is installed with Devicemapper, which triggers a warning for UCP install and is not recommended for production. I need to reboot the machine after installing headers, then continue setup after reboot.
* enhancement: install-ucp.sh on replicas waits for master-ip:443 to respond before installing. I had to add a 3 min sleep buffer to make sure the master was operational. It seems checking master-ip:443/_ping, version of info would od the trick.
* limitation: the template is designed for Ubuntu 14.04.3-LTS. If you change the linuxSku you need to change the part of the Docker CS install script in install-ucp.sh to reflect that. Also it relies on cloud-init, which behavior may change in other distributions and versions.
* limitation: the load balancer targeting the VM scale set is useless, since your containers are going to be exposed at many different ports. Moreover, VM scalesets are designed to run uniform workloads, load balanced all in the same way (there's one virtual NIC for the whole set, you cannot be specific about which VM your Azure load balancer targets), which does not fit with the container model. I'm looking into how to make interlock work with nginx on a dedicated pool of VMs and create an interlock plugin that would provision public IPs and network rules for each group of containers scheduled with the same --hostname. Another option would be to use regular VMs instead of the scale set and create an interlock plugin that provisions public IPs and Azure load balancers on demand. I'm open to suggestions there.

## Development tips

During development, the [Azure resource Explorer](https://resources.azure.com/) is your best tool to understand what got provisioned, and how deployment went. Here is a [bit of context](https://azure.microsoft.com/en-us/blog/azure-resource-explorer-a-new-tool-to-discover-the-azure-api/) and [Scott Hanselman's take on it](http://www.hanselman.com/blog/IntroducingAzureResourceExplorerForTheAzureResourceManagementAPIs.aspx).  

Once your cluster is deployed, you can check how things went by sshing into the machines. The logs of the UCP install are in `/var/log/azure/cluster-bootstrap.log`:
```
ssh -p 2200 azureuser@[masterFQDN]
sudo docker info
sudo docker ps
sudo cat /var/log/azure/cluster-bootstrap.log
ssh -p 2201 azureuser@[masterFQDN]
... 
ssh -p 50000 azureuser@[agentFQDN]
...
ssh -p 50001 azureuser@[agentFQDN]
```  

`cat docker_subscription.lic|gzip|base64` and `cat install-ucp.sh.lic|gzip|base64` then copying and pasting in azuredeploy.parameters.json is tiresome. write a script to do that from a template is left as an exercise to the reader:-)

## Acknowledgements

Thanks to @ehazlett & UCP team for their advice on how to setup UCP.

Thanks to @rgardler, @gossmanster, @anhowe, @gbowerman and the Azure team for provising the base template and their help on how to tweak it.
  

