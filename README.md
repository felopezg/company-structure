 
# Set-up

This projects requires Java 8 or later, Apache Maven 3 or later, and a working Docker set-up.

# Maven + Docker

This project uses Spotify’s Dockerfile Maven plugin to integrate Maven integrate with Docker,  although the Docker image build and push phases are not bound.

To get the Docker image execute the following from the commandline:

`mvn dockerfile:build`

To push the Docker image execute the following from the commandline:

`mvn dockerfile:push`

# Docker 

To create and start a container execute the following from the commandline:

`docker-compose up`
