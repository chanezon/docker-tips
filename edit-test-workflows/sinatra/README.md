# Sinatra sample app

This sample app demonstrates how to use Docker for interactive edit/test cycles while developing a Sinatra application.

The project contains 2 Dockerfiles: Dockerfile for the image of your microservice for production, that will be pushed to Docker Hub. Dockerfile.dev, to be used in development. Dockerfile.dev uses [rerun](http://www.sinatrarb.com/faq.html#reloading) to enable the app to reload when you change application files.

To develop the app:
```
docker build -t sinapp .
docker build -t sinappdev -f Dockerfile.dev .
docker run -d -v $(pwd):/app -p 3000:3000 sinappdev
```

Then you can edit files in your local directory, save and shift-reload in your browser to see the changes in action. For example edit hello.rb and change the message, then `curl <hostname>:3000`.

When you have tested your app and it works, build the non dev version and push it to hub.:
```
docker build -t <hub-username>/sinapp .
docker push <hub-username>/sinapp
```

