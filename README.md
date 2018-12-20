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

# Usage

Get a node (Organization Unit):

`HTTP-GET`

`http://localhost:8080/v1.0.0/organization-units/[id]`

Get a node's children (Organization Unit's dependants):

`HTTP-GET`

`http://localhost:8080/v1.0.0/organization-units/[id]`

Replace a node´s parent with an existing node in the tree (HTTP 400 if it creates a cycle):

`HTTP-PUT`

`http://localhost:8080/v1.0.0/organization-units/[id]`

`{"organizationUnit": [id-will-report-to] }

Replace a node´s parent with a new node:

`HTTP-POST`

`http://localhost:8080/v1.0.0/organization-units/[id]`

`{"organizationUnit": -1 }







