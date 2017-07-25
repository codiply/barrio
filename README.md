
[![Travis CI Status](https://travis-ci.org/codiply/barrio.svg?branch=master)](https://travis-ci.org/codiply/barrio)

## Barrio

Distributed approximate nearest neighbor search

### Prerequisites

- Install [Scala Simple Build Tool (SBT)](http://www.scala-sbt.org/download.html)
- Install [Docker Compose](https://docs.docker.com/compose/install/)

### Run single node

Run

    sbt "run -f path-to-input-data-filename"
    

You can make a `POST` request to [http://localhost:18001/neighbors](http://localhost:18001/neighbors).
For example, to get the 3 nearest neighbors from the origin, the request is

    {
      "coordinates": [0.0, 0.0, 0.0],
      "k": 3
    }

### Run cluster

First package the whole project into one single `.jar`

    sbt assembly
    
and then start the cluster


    docker-compose up -d
    
Seee the logs with

    docker-compose logs
    
Bring the cluster down with

    docker-compose down 