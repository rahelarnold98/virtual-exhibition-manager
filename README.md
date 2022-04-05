# Virtual Reality Exhibition Manager (VREM)

[![Build Status](https://travis-ci.org/VIRTUE-DBIS/virtual-exhibition-manager.svg?branch=master)](https://travis-ci.org/VIRTUE-DBIS/virtual-exhibition-manager)

The Virtual Reality Exhibition Manager (VREM) is a tool that allows for the configuration, generation, storage of and
access to VR exhibition definitions.   
While VREM acts as a back end, the exhibitions can be viewed in
the [Virtual Exhibition Presenter (VREP)](https://github.com/dbisUnibas/virtual-exhibition-presenter) (Unity).  
For the Angular UI see [VREM-UI](https://github.com/sauterl/virtual-exhibition-manager-ui). It has been created as part
of the [Open Cultural Data Hackathon 2018](http://make.opendata.ch/wiki/event:2018-10), held in Zurich, Switzerland.

## Prerequisites

Below is a very shortened version of
the [setup guide](https://github.com/VIRTUE-DBIS/virtual-exhibition-presenter/wiki/Setup-Guide) on how to set up VREM.

### System dependencies

* JDK 11 or higher
* You will require [MongoDB](https://docs.mongodb.com/manual/installation/) as storage engine. We recommend
  using [Docker](https://www.docker.com). If you have installed docker, you can start a new container
  using `docker run --name vrem-container -d -p 27017:27017 mongo`. If you already have a container, restart it
  using `docker start mongo`. If you are on a unix-system, use the `launch_db.sh` script.
* If you want VREM to be able to dynamically generate exhibition rooms for an image collection, you also need a running
  instance of [Cineast](https://github.com/vitrivr/cineast) and
  [Cottontail DB](https://github.com/vitrivr/cottontaildb). Consult
  the [setup guide](https://github.com/VIRTUE-DBIS/virtual-exhibition-presenter/wiki/Setup-Guide) for additional
  information.

## Building VREM

VREM can be built using [Gradle](http://gradle.org/):

```
 ./gradlew clean deploy
 cd build/libs
 java -jar virtual-exhibition-manager-3.0.0-SNAPSHOT.jar
 java -jar virtual-exhibition-manager-3.0.0-SNAPSHOT.jar <command>
 ```

Make sure you have the correct working directory set so VREM can properly import exhibitions and serve content.

## Starting a server

Before starting, you should adapt the configurations in your config.json file (see example file).  
After doing so, you may serve stored exhibitions by running VREM with the following command:

```
 java -jar virtual-exhibition-manager-3.0.0-SNAPSHOT.jar server -c /path/to/your/config.json
```

## Importing an exhibition

By using the `import-folder` command, you can import a stored exhibition to the running MongoDB instance in order to be
able to serve the exhibition via the `server`command afterwards.  
The following command imports a collection from the `data/import/my_exhibition` folder (relative to VREM's directory):

```
import-folder --config=config.json --path=data/import/my_exhibition --name=demo
```

Since the image and ambient audio files are not stored in the database, VREP will maintain a folder containing the
exhibition images in `data/` (relative to VREM).

For an exhibition example, consult the repository
at [https://github.com/VIRTUE-DBIS/vre-mixnhack19](https://github.com/VIRTUE-DBIS/vre-mixnhack19).
