# Spring Boot Demo - Authorization Server (with MongoDB), Resource Server (with MongoDB) and Web Client

## Prerequisites

Before you can use the Authorization Server you'll need to add
the following line to `/etc/hosts`:

```
127.0.0.1  spring-demo-authsrv
```

### Docker Only

If you want to run the applications only in a Docker environment
you'll need nothing more than

- Docker (or Docker Desktop): [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)
- Docker-Compose
  - included in Docker Desktop for macOS
  - on Linux: [https://docs.docker.com/compose/install/](https://docs.docker.com/compose/install/)

### Execute directly on host machine

- OpenJDK 18
- MongoDB: either native application or via Docker (see below)
- Recommended IDE: IntelliJ Ultimate


## MongoDB + Mongo Express in Docker Containers

Both the Authorization and Resource Server need a running MongoDB instance.  
You can use the MongoDB Docker-Compose environment that comes with this repository.  
To start MongoDB and Mongo Express (Database Web Frontend) run:

```
$ ./docker-mongodb/dc-db-mongo.sh up
```

And to stop the Docker containers run:

```
$ ./docker-mongodb/dc-db-mongo.sh down
```

---

Starting the Docker-Compose environment will

- create a fresh MongoDB instance with no data in it
- create the root user
- create the user for the Authorization Server
- create the user for the Resource Server
- and start a Mongo Express instance which can then be used to look inside the DB

---

#### MongoDB

The MongoDB server will run on port 27017.

#### Mongo Express

Mongo Express web interface: [http://127.0.0.1:8070/](http://127.0.0.1:8070/)


## Run the apps in a Docker Containers

To start all apps at once run:

```
$ ./docker-run_apps/dc-run_apps.sh up
```

And to stop all the apps run:

```
$ ./docker-run_apps/dc-run_apps.sh down
```

**Please note** that when starting the applications it can **take quite a while** until everything
is up and running. One way of monitoring the activities in the individual Docker containers
is to run:

```
$ docker stats
```

To see the log output of all containers you can run:

```
$ ./docker-run_apps/dc-run_apps.sh logs -f
```


## Run the apps directly on your host machine

### Applications (Authorization Server + Resource Server + Web Client)

You can either use your IDE to run the apps or run them manually from a terminal.

If you want to run them from a terminal I recommend to actually use three terminals:

- Terminal 1: `$ ./gradlew :mod_auth_server:run`
- Terminal 2: `$ ./gradlew :mod_resource_server:run`
- Terminal 3: `$ ./gradlew :mod_webclient:run`


## Authorization Server

The Authorization Server is responsible for

- managing the **Users**
- managing the **OAuth2 Clients**
- managing the granted **OAuth2 Access Tokens**

URL: [http://spring-demo-authsrv:9000/](http://spring-demo-authsrv:9000/)  
(The auth server has a web frontend)

All the configuration can be found in  
`mod_auth_server/src/main/resources/application.properties`

Default Users (can be found in `application.properties`):

| Username               | Password |
|------------------------|----------|
| admin@springdemo.org   | pw       |
| regular@springdemo.org | pw       |
| nobody@springdemo.org  | pw       |


## Resource Server

The Resource Server is responsible for

- managing the **Resources** that clients can access
- performing **Access Control** for those resources

URL: `http://127.0.0.1:8090`  
(The resource server doesn't have a web frontend)

All the configuration can be found in  
`mod_resource_server/src/main/resources/application.properties`


## Web Client

The Web Client has a pretty ugly UI but it allows us the demonstrate some
features of Java Spring and how to access data from the Authorization and Resource Servers.

URL: [http://127.0.0.1:8080/](http://127.0.0.1:8080/)  

All the configuration can be found in  
`mod_webclient/src/main/resources/application.properties`


## Further Reading

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.7.1/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.7.1/gradle-plugin/reference/html/#build-image)
* [Spring Session](https://docs.spring.io/spring-session/reference/)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/docs/2.7.1/reference/htmlsingle/#using.devtools)
* [Spring Security](https://docs.spring.io/spring-boot/docs/2.7.1/reference/htmlsingle/#web.security)
* [Spring Data MongoDB](https://docs.spring.io/spring-boot/docs/2.7.1/reference/htmlsingle/#data.nosql.mongodb)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.7.1/reference/htmlsingle/#web)

### Guides
The following guides illustrate how to use some features concretely:

* [Securing a Web Application](https://spring.io/guides/gs/securing-web/)
* [Spring Boot and OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
* [Accessing Data with MongoDB](https://spring.io/guides/gs/accessing-data-mongodb/)
* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
