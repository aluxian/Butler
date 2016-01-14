# ButlerCloud 

The main server of Butler (a.k.a. the brain). It handles messages received from Butler clients through GCM.

[![Build Status](https://api.shippable.com/projects/54358d3d7a7fb11eaa64b8f3/badge?branchName=master)](https://app.shippable.com/projects/54358d3d7a7fb11eaa64b8f3/builds/latest)

## Features

- Fast, concurrent, scalable architecture based on Akka and the actor model.

## Environments

- Dev
- Test
- Production (don't run this at home)

## Continuous Deployment

- The code is automatically built and tested (unit/integration tests w/ coverage) by shippable.com every time a new commit is detected.
- Shippable.com then pushes the code onto the production servers through AWS OpsWorks.

## How to run

Use the SBT CLI:

    $ sbt run

Make sure you have set your JAVA_HOME env var to JDK 8.

## Databases

The server uses 2 databases:

- MongoDB
- Neo4j
