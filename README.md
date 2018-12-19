 
# Set-up

This projects requires Java 8 or later, Apache Maven 3 or later, and a working Docker set-up.

# Maven + Docker

he project uses Spotify’s Dockerfile Maven plugin to integrate Maven with Docker,  although the Docker image build and push phases are not bound.
To get the Docker image run:

`mvn dockerfile:build`

To push the Docker image run:

`mvn dockerfile:push`

# Docker 

To create and start a container run:

`docker-compose up`
