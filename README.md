# Deletion service

Service for orchestration of custom processes.

## Table of Contents

- [Introduction](#introduction)
- [Features](#features)
- [Requirements](#requirements)
- [Quick Start](#quick-start)

## Introduction

Deletion service is a test service for Spring state machine framework. It's only a test project, cannot be launched

## Features
 * Spring persisted state machine
 * Kafka consumers

## Requirements

The application can be run locally or in a docker container, 
the requirements for each setup are listed below.

### Local

* [Java 17 SDK](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
* [Gradle >= 7](https://gradle.org/install/)

### Docker

* [Docker](https://www.docker.com/get-docker)

## Quick Start

Application will run by default on port `11003`

Configure the port by changing `server.port` in __application.properties__


### Run Local

You can run application either via Intellij launch configuration (preferred way) or
manually
```bash
$ .\gradlew bootRun --args='--spring.profiles.active=dev'
```

### Run Docker

Use `docker-compose.yml` to build the image and create a container.
Note, that by default container will run using `application-dev.properties`

### Run code quality assurance tasks

If you want to get total coverage and sonar analysis with local changes, then you should run following tasks:
```
./gradlew test jacocoTestReport
./gradlew sonar
```
Then, jacoco test report with coverage will be generated on local machine in build folder
and sonar analysis will take place on server and will be visible on sonarcloud instance.
Also, it is recommended to install SonarLint Intellij plugin for integration of code
quality analysis more native-like
Also, there is a possibility to configure jacoco coverage as a replace for common Idea coverage analyzer (it's optional)