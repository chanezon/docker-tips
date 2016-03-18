FROM sinapp
MAINTAINER Patrick Chanezon <chanezon@docker.com>

RUN gem install rerun
VOLUME $APP_HOME
# Start server
CMD ["rerun", "ruby", "app.rb"]
