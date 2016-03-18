FROM ruby:2.3.0
MAINTAINER Patrick Chanezon <chanezon@docker.com>

# Install gems
ENV APP_HOME /app
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY Gemfile* $APP_HOME/
RUN bundle install

# Upload source
COPY . $APP_HOME

# Start server
ENV PORT 3000
EXPOSE 3000
CMD ["ruby", "app.rb"]