# Spring Boot Demo - Authorization Server (with MongoDB), Resource Server (with MongoDB) and Web Client

## Prerequisites

- OpenJDK 18
- Recommended IDE: IntelliJ Ultimate
- MongoDB
- entry in `/etc/hosts`

Before you can use the Authorization Server you'll need to add
a line to `/etc/hosts`:

```
127.0.0.1  auth-server
```

And you'll need a running MongoDB instance for the Authorization and Resource Server.  
I recommend using a simple Docker container for that.  

You can use the Docker-Compose environment that comes with this repository.  
Simply run

```
$ ./docker-mongodb/dc-db-mongo.sh up

or

$ cd docker-mongodb
$ docker-compose up --detach
```

and to stop the Docker Container run

```
$ ./docker-mongodb/dc-db-mongo.sh down

or

$ cd docker-mongodb
$ docker-compose down
```

---

Starting the Docker-Compose environment will

- create a fresh MongoDB instance with no data in it
- create the root user
- create the user for the Authorization Server
- create the user for the Resource Server
- and start a Mongo Express instance which can then be used to look inside the DB

## Mongo Express

When using the Docker-Compose environment that comes with this repository
you can reach the web interface of Mongo Express here:  
[http://127.0.0.1:8070/](http://127.0.0.1:8070/)

## Authorization Server

@TODO

## Resource Server

@TODO

## Web Client

@TODO
