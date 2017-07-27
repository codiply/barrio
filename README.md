
[![Travis CI Status](https://travis-ci.org/codiply/barrio.svg?branch=master)](https://travis-ci.org/codiply/barrio)

# Barrio

Distributed approximate nearest neighbor search in Scala/Akka

## Development environment

### Prerequisites

- Install [Scala Simple Build Tool (SBT)](http://www.scala-sbt.org/download.html)
- Install [Docker Compose](https://docs.docker.com/compose/install/)
- Your favourite IDE for Scala

### Run single node

Run

    sbt "run -f path-to-input-data-file"
    

You can make a `POST` request to [http://localhost:18001/neighbors](http://localhost:18001/neighbors).
For example, to get the 3 nearest neighbors from the origin, the request is

    {
      "coordinates": [0.0, 0.0, 0.0],
      "k": 3,
      "distanceThreshold": 1.0
    }
    
The `distanceThreshold` defines an area around the given location that will be searched thoroughly. 
Note that if this threshold and the number of neighbors `k` are too big, the search might time out, 
returning the best results up to this point without any guarantees.

### Run cluster in docker

First package the whole project into one single `.jar`

    sbt assembly
    
and then start the cluster

    docker-compose up -d
    
See the logs with

    docker-compose logs
    
Bring the cluster down with

    docker-compose down 
    
### Environment variables for node configuration

- `AKKA_SYSTEM`: a consistent name across nodes that belong to one cluster
- `CONTAINER_HOSTNAME`: the hostname of this node
- `AKKA_REMOTING_PORT`: the port to be used by AKKA on this node
- `AKKA_SEED_HOST`: the hostname of the seed node
- `AKKA_SEED_PORT`: the port used by AKKA on the seed node