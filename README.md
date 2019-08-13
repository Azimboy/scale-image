# Scale Image

This application is based on [PlayFramework](https://www.playframework.com/) and uses a [Srcimage](https://github.com/sksamuel/scrimage) library to modify images.

**Project includes following:**
- Ability to upload multiple files from UI
- Ability to upload multiple files from API request.
- Ability to accept multipart/form-data requests.
- Ability to accept JSON requests with BASE64 encoded images.
- Ability to upload images at a given URL (image posted somewhere on the Internet).
- Create a square preview of the image for a given width and height (default size 100px by 100px).
- Tests (Unit, Browser, Functional and Integration).
- Docker integration

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing 
purposes.

## Development Dependencies

```
scala
```

```
sbt
``` 

```
docker
```

## Running The Application

### From The Terminal

To run the application please invoke the following command:

```
sbt run
```
This will start the server in dev mode listening on port 9000. 

```
$ sbt run
[info] Loading settings for project global-plugins from idea.sbt ...
[info] Loading global plugins from /.sbt/1.0/plugins
[info] Updating ProjectRef(uri("file:/.sbt/1.0/plugins/"), "global-plugins")...
[info] Done updating.
[info] Loading settings for project scale-image-build from plugins.sbt ...
[info] Loading project definition from /IdeaProjects/scale-image/project
[warn] There may be incompatibilities among your library dependencies; run 'evicted' to see detailed eviction warnings.
[info] Loading settings for project squarepreview from build.sbt ...
[info] Set current project to scale-image (in build file:/IdeaProjects/scale-image/)

--- (Running the application, auto-reloading is enabled) ---

[info] p.c.s.AkkaHttpServer - Listening for HTTP on /0:0:0:0:0:0:0:0:9000

(Server started, use Enter to stop and go back to the console...)

``` 

### Docker

To run in production mode inside a Docker container simply run command below:

```
docker-compose up
```

You can access to web UI via url:

```
http://localhost:9000 
```

## Running The Tests

### From The Terminal

To execute the entire test suite run this command:

```
sbt test
```

Test results:

```
[info] ScalaTest
[info] Run completed in 1 minute, 34 seconds.
[info] Total number of tests run: 46
[info] Suites: completed 6, aborted 0
[info] Tests: succeeded 46, failed 0, canceled 0, ignored 0, pending 0
[info] All tests passed.
[info] Passed: Total 46, Failed 0, Errors 0, Passed 46
[success] Total time: 118 s, completed Aug 13, 2019 2:28:22 PM
```

## API documentation

#### POST /api/file-upload?width=100&height=100

```
localhost:9000/api/file-upload?width=100&height=100
```

Accepts `multipart/form-data` requests with multiple files and returns image urls as JSON

---

#### POST /api/data-upload?width=100&height=100

```
localhost:9000/api/data-upload?width=100&height=100
```

Accepts `application/json` requests with Dase64 encoded file bytes and returns image urls as JSON

Data Example:

```
{
  "file": [
    { "content": "iVBORw0KGgoAAAANSUhEUgAAAMoAAACyCAYAAAADFFAEAAAABHNCSVQICAgIfAhkiAAAABl0RVh0" },
    { "content": "iVBORw0KGgoAAAANSUhEUgAAAMoAAACyCAYAAAADFFAEAAAABHNCSVQICAgIfAhkiAAAABl0RVh0" },
  ]
}
```
---

#### POST /api/from-url?url=image_url&width=100&height=100

```
localhost:9000/api/from-url?url=image_url&width=100&height=100
```

Accepts image url as query parameter and returns image url as JSON

---