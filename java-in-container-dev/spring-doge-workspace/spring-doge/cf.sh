#/bin/zsh


## Deploy the Doge application to Cloud Foundry 

cf d doge 

# setup the MongoLabs Mongo service
cf cs mongolab sandbox doge-mongo

# push the application and defer to the manifest.yml to handle the rest 
cf push

# make sure that - if we're assinging random-word based URIs - we don't accrue unused routes.
cf delete-orphaned-routes -f

# list all the apps 
cf apps
