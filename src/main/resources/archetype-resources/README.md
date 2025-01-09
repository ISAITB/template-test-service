#set($hash = '#')
${hash} Introduction

This application implements the [GITB test service APIs](https://www.itb.ec.europa.eu/docs/services/latest/) in a
[Spring Boot](https://spring.io/projects/spring-boot) web application that is meant to support
[GITB TDL test cases](https://www.itb.ec.europa.eu/docs/tdl/latest/) running in the Interoperability Test Bed. 

${hash}${hash} Messaging service implementation

The sample messaging service is used by the Test Bed to send and receive a text message. When told to `send` a message
this service simply logs it. Regarding received messages, these are provided via HTTP GET call upon which time the
appropriate active test sessions get notified via callback. To manually complete a pending 'receive' call, make a GET
request to http://localhost:8080/input?message=MESSAGE&session=SESSION in which you set the 'MESSAGE' placeholder to the
text to send back, and the 'SESSION' placeholder to the test session ID to notify. Note that the 'session' parameter can
be altogether skipped to notify all pending test sessions.

Once running, the messaging endpoint's WDSL is available at http://localhost:8080/services/messaging?WSDL. See
[here](https://www.itb.ec.europa.eu/docs/services/latest/messaging/) for further information on messaging service implementations.

${hash}${hash} Processing service implementation

The sample processing service is used by the Test Bed to lowercase or uppercase a given text input.

Once running, the processing endpoint's WDSL is available at http://localhost:8080/services/process?WSDL. See 
[here](https://www.itb.ec.europa.eu/docs/services/latest/processing/) for further information on validation service implementations.

${hash}${hash} Validation service implementation

The sample validation service validates a text against an (also provided) expected value. The user of the service can
also select whether he/she wants to have a mismatch reported as an error or a warning. Finally, an information message
is also returned in case values match but when ignoring casing. 

Once running, the validation endpoint's WDSL is available at http://localhost:8080/services/validation?WSDL. See 
[here](https://www.itb.ec.europa.eu/docs/services/latest/validation/) for further information on processing service implementations.

${hash} Prerequisites

The following prerequisites are required:
* To build: JDK 17+, Maven 3.8+.
* To run: JRE 17+.

${hash} Building and running

1. Build using `mvn clean package`.
2. Once built you can run the application in two ways:  
  a. With maven: `mvn spring-boot:run`.  
  b. Standalone: `java -jar ./target/${artifactId}-${version}.jar`.

${hash}${hash} Live reload for development

This project uses Spring Boot's live reloading capabilities. When running the application from your IDE or through
Maven, any change in classpath resources is automatically detected to restart the application.

${hash}${hash} Packaging using Docker

Running this application as a [Docker](https://www.docker.com/) container is very simple as described in Spring Boot's
[Docker documentation](https://spring.io/guides/gs/spring-boot-docker/). The first step is to 
[Install Docker](https://docs.docker.com/install/) and ensure it is up and running. You can now build the Docker image
using the approach that best suits you. Note that in both cases you can adapt as you want the resulting image name.

**Option 1: Using the provided Dockerfile** 

First make sure you build the app by issuing `mvn package`. Once built you can create the image using:
```
docker build -t local/${artifactId} --build-arg JAR_FILE=./target/test-service-1.0-SNAPSHOT.jar .
```

**Option 2: Using the Spring Boot Maven plugin**
```
mvn spring-boot:build-image -Dspring-boot.build-image.imageName=local/${artifactId}
```

${hash}${hash}${hash} Running the Docker container

Assuming an image name of `local/${artifactId}`, it can be ran using `docker run --name ${artifactId} -p 8080:8080 -d local/${artifactId}`.