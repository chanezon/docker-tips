# Azure Resource Manager Template to provision a Docker Universal Control Plane cluster

This template is evolved from Microsoft's Azure Container Service base template to provision Swarm cluster. I modified it to provision a Docker Universal Control Plane cluster.

Steps:
* Install azure cli
* login
* get a ucp license
* edit azuredeploy.parameters.json to parametrize your cluster
  * masterEndpointDNSNamePrefix and agentEndpointDNSNamePrefix will be public dns names in Azure where you cluster is exposed
  * ucpInstallScriptContent lets you customize the UCP installation script if you need to
  * dockerLicenceData paste the result of cat install-ucp.sh|gzip|base64
* create your group and deploy the template with your own values for the resource group name and deployment

```  
azure group create -n "patucp"  -l "West US"
azure group deployment create "patucp1" "patucp1dep"  -f azuredeploy.json -e azuredeploy.parameters.json
```

15 minutes later you should have a working UCP cluster with 3 masters and 1 node. Nodes are in a VM Scale Set so you should be able to add a rule to autoscale that (I still need to test that).

The template outputs the FQDN of the master. In your browser, open it on https port: https://<masterFQDN>. Ignore Chrosme's warning and say you want to proceed anyway. You shoudl end up in UCP login screen. Default credentials are admin/orca. Don't forget to change them as soon as you are logged in.

Have fun with UCP!



