# quarkus-cache Project

POC to show how a cache can be invalidated in regular intervals.

## Configure the application
Configuration property `cache.greeting.invalidate-every` (Duration) controls how often the cache is invalidated. It also controls the initial delay after application startup until the cache is invalidated for the first time. It is configured to `30s`

## Starting the application
Run `quarkus:dev`

## Testing the behaviour
Access http://localhost:8080/hello. The first request should take about 2s until it responds. Subsequent requests (<kbd>F5</kbd>) should respond instantly up until the scheduled job runs, and one request is slow again.

## Analyzing the logs
When we take a look at the application logs, we see log entries similar to:
```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
2023-01-17 23:33:04,455 INFO  [io.quarkus] (Quarkus Main Thread) quarkus-cache 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.15.3.Final) started in 1.398s. Listening on: http://localhost:8080
2023-01-17 23:33:04,465 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-01-17 23:33:04,465 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cache, cdi, config-yaml, resteasy-reactive, scheduler, smallrye-context-propagation, vertx]
2023-01-17 23:33:06,289 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:06,297 INFO  [de.tur.GreetingResource] (executor-thread-0) expensive method called
2023-01-17 23:33:09,172 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:10,407 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:12,342 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:12,653 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:35,005 INFO  [de.tur.GreetingResource] (vert.x-worker-thread-0) cache invalidated
2023-01-17 23:33:37,331 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:37,332 INFO  [de.tur.GreetingResource] (executor-thread-0) expensive method called
2023-01-17 23:33:43,519 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:44,075 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:33:44,495 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
```

As we can see, the expensive is only called
- after application startup, or
- after the cache has been invalidated.