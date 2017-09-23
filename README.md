
[![Travis CI Status](https://travis-ci.org/codiply/barrio.svg?branch=master)](https://travis-ci.org/codiply/barrio)

# Barrio

Distributed approximate nearest neighbor search in Scala/Akka

## Development environment

### Prerequisites

- **[Scala Simple Build Tool (SBT)](http://www.scala-sbt.org/download.html)**:
  Used for building the project, running the tests and running the application as a single node.
- **[Docker Compose](https://docs.docker.com/compose/install/)**:
  Used for running the application in a cluster of nodes. Each node is in a separate docker container.
- **Your favourite IDE for Scala (for example [ScalaIDE for Eclipse](http://scala-ide.org/))**
- **A Scalastyle plugin for your IDE (for example [Eclipse Scalastyle plugin](http://www.scalastyle.org/eclipse-getting_started.html))**:
  Used for checking for style errors in scala code. The configuration is in `scalastyle-config.xml`. 
  Alternatively, you can run `sbt ss` from the command line.

### Checking for Scalastyle errors

Run from the command line 

- `sbt scalastyle` or
- `sbt test:scalastyle` for checking the test files, or
- `sbt ss` for checking all files

### Input file format

Each line contains one data point, and  is split into 3 parts using a separator that defaults to three colons (`:::`)

    <id>:::<comma separated coordinates>:::<additional data>

- The first part is a unique string that identifies the point. 
- The second part contains the coordinates separated by a different separator that defaults to comma (`,`).
- The third part is a string that can contain additional data for the specific data point.

### Run single node

Run

    sbt 'run -f <path to input data file> -d <number of dimensions>'
    
or 

    sbt 'run --isUrl -f <url to input data file> -d <number of dimensions>'
    

This will start the server on [http://localhost:18001](http://localhost:18001).

You can specify the two separators with the following arguments

    --separator "@~@" --coordinateSeparator "$"


### Run cluster in docker

First package the whole project into one single `.jar`

    sbt assembly
    
and then start the cluster

    docker-compose up -d
    
This uses the configuration in `docker-compose.yml` and it will create a cluster of 4 nodes. 
Their endpoints are accessible on the host machine here:

- Node 1: [http://localhost:19001](http://localhost:19001)
- Node 1: [http://localhost:19002](http://localhost:19002)
- Node 1: [http://localhost:19003](http://localhost:19003)
- Node 1: [http://localhost:19004](http://localhost:19004)
    
See the logs with

    docker-compose logs
   
and [grep](https://en.wikipedia.org/wiki/Grep) any specific messages. 
    
Bring the cluster down with

    docker-compose down 
    
### Environment variables for node configuration

- `BARRIO_AKKA_SEEDS`: comma-separated list of the seed nodes in the format `host:port` (do not include any protocol or actor system information)
- `BARRIO_AKKA_SYSTEM`: a consistent name across nodes that belong to one cluster
- `BARRIO_AKKA_REMOTING_PORT`: the port to be used by AKKA on this node
- `BARRIO_DEFAULT_REQUEST_TIMEOUT`: the default timeout for each request in milliseconds
- `BARRIO_HOSTNAME`: the hostname of this node
- `BARRIO_MAX_REQUEST_TIMEOUT`: the maximum timeout allowed to be set in a request
- `BARRIO_WEB_API_PORT`: the port of the Web API endpoint
- `CACHE_EXPIRE_AFTER_ACCESS_SECONDS`: expiry period in seconds for cached responses
- `CACHE_MAXIMUM_SIZE`: the maximum number of responses to be cached
- `AWS_CREDENTIAL_PROFILES_FILE`: Used for AWS credentials if you load the data from S3. (see [here](http://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html) for more details)

### default values

- `BARRIO_AKKA_SEEDS`: `"localhost:18011"`
- `BARRIO_AKKA_SYSTEM`: `"barrio"`
- `BARRIO_AKKA_REMOTING_PORT`: `18011`
- `BARRIO_DEFAULT_REQUEST_TIMEOUT`: `10000`
- `BARRIO_HOSTNAME`: `"localhost"`
- `BARRIO_MAX_REQUEST_TIMEOUT`: `60000`
- `BARRIO_WEB_API_PORT`: `18001`
- `CACHE_EXPIRE_AFTER_ACCESS_SECONDS`: 1200
- `CACHE_MAXIMUM_SIZE`: 10000

## API

### Get neighbors by location

To get neighbors  make a `POST` request to `/neighbors/`.
For example, to get the 3 nearest neighbors from the origin, the request is

    {
      "location": [0.0, 0.0, 0.0],
      "k": 3,
      "distanceThreshold": 1.0
    }
    
The `distanceThreshold` defines an area around the given location that will be searched thoroughly. 
Note that if this threshold and the number of neighbors `k` are too big, the search might time out, 
returning the best results up to this point without any guarantees. The default value is `0.0`.

If you prefer to get all results found in a limited amount of time, you can set the timeout (in milliseconds) in the request

    {
      "location": [0.0, 0.0, 0.0],
      "k": 3,
      "distanceThreshold": 10.0,
      "timeout": 1000
    }
    
You can request to get the coordinates of each neighbor and/or its additional data

    {
      "location": [0.0, 0.0, 0.0],
      "k": 3,
      "distanceThreshold": 10.0,
      "includeLocation": true,
      "includeData": true
    }
    
both of which default to `false`. Depending on the number of dimensions and the size of the additional data, this might make the response large.
    
### Get neighbors by location Id

Alternatively, you can obtain neighbors around a given known location identified by its Id by `POST`-ing to the same resource`/neighbors/`

    {
      "locationId": "point-1",
      "k": 3
    }

All settings used in getting neighbors by location apply here too.

### Debugging

You can get cluster statistics via a `GET` request to `/stats/`.

You can check for errors and discrepancies between the nodes in the cluster via a `GET` request to `/health/`.

## Demo Heroku App

You can try the API on [this demo app](http://barrio-movies.herokuapp.com/) on [Heroku](http://www.heroku.com). 
The input data are the item factors produced by the ALS algorithm on the [MovieLens dataset](https://movielens.org/). 
The item factors are features for each movie in a 16-dimensional space. They allow us to search for similar movies.
The id used is the [IMDB id](http://www.imdb.com/) of a movie 
(it is the numeric part at the end of the url for a specific movie).

To perform a search make a `POST` request to [http://barrio-movies.herokuapp.com/neighbors](http://barrio-movies.herokuapp.com/) like this

    {
      "locationId": "0068646",
      "k": 20,
      "distanceThreshold": 0.03,
      "includeLocation": false,
      "includeData": true
    }


