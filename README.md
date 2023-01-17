# quarkus-cache Project

POC to show how a cache can be invalidated in regular intervals.

## Configure the application
Configuration property `quarkus.cache.caffeine."greeting".expire-after-write` (Duration) controls how often the cache is invalidated. It is configured to `30s`

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
2023-01-17 23:51:33,725 INFO  [io.qua.sch.run.SimpleScheduler] (Quarkus Main Thread) No scheduled business methods found - Simple scheduler will not be started
2023-01-17 23:51:33,793 INFO  [io.quarkus] (Quarkus Main Thread) quarkus-cache 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.15.3.Final) started in 1.419s. Listening on: http://localhost:8080
2023-01-17 23:51:33,793 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-01-17 23:51:33,794 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cache, cdi, config-yaml, resteasy-reactive, scheduler, smallrye-context-propagation, vertx]
2023-01-17 23:51:43,138 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:51:43,147 INFO  [de.tur.GreetingResource] (executor-thread-0) expensive method called
2023-01-17 23:51:47,209 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:51:48,684 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:51:48,891 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:51:49,071 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:51:49,215 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:52:23,125 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-17 23:52:23,126 INFO  [de.tur.GreetingResource] (executor-thread-0) expensive method called
```

As we can see, the expensive is only called
- after application startup, or
- after the cache has been invalidated.