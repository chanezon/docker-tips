# Sinatra sample app

This sample app demonstrates how to use Docker for interactive edit/test cycles while developing a Sinatra application.

The project contains 2 Dockerfiles: Dockerfile for the image of your microservice for production, that will be pushed to Docker Hub. Dockerfile.dev, to be used in development. Dockerfile.dev uses [rerun](http://www.sinatrarb.com/faq.html#reloading) to enable the app to reload when you change application files.

To develop the app:
```
docker build -t sinapp .
docker build -t sinappdev -f Dockerfile.dev .
docker run -d -v $(pwd):/app1 -p 3000:3000 sinappdev
```

Then you can edit files in your local directory, save and shift-reload in your browser to see the changes in action. For example edit hello.rb and change the message, then `curl <hostname>:3000`.


