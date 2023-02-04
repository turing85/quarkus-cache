# quarkus-cache Project

POC to show how a cache can be invalidated in regular intervals and refreshed through a configurable cron expression.

## Configure the application
Configuration property `quarkus.cache.caffeine."greeting".expire-after-write` (Duration) controls how often the cache is invalidated. It is configured to `10s`

Configuration property `quarkus.cache.caffeine."greeting".refresh-cron` configures the cron expression to control cache refresh (invalidation and load). It se configured to `0/20 * * * * ? *`, i.e. at 0, 20 and 40 seconds, every minute, hour, day-of-month, ...

## Technical details
Looking at class `CachedGreetingProvider`, we see that it is annotated with:
* `@Startup` to construct the bean at program startup (i.e. non-lazily, this will then through the constructor pre-heat the cache)
* `@PostConstruct` on the `preheatCache()`-method to initialize the cache values

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
2023-01-21 00:08:37,974 WARN  [io.qua.config] (Quarkus Main Thread) Unrecognized configuration key "quarkus.cache.caffeine.greeting.refresh-cron" was provided; it will be ignored; verify that the dependency extension for this configuration is set or that you did not make a typo
2023-01-21 00:08:38,270 INFO  [de.tur.imp.CachedGreetingProvider] (Quarkus Main Thread) calling expensive method
2023-01-21 00:08:40,271 INFO  [de.tur.imp.CachedGreetingProvider] (Quarkus Main Thread) expensive method called
2023-01-21 00:08:40,334 INFO  [io.quarkus] (Quarkus Main Thread) quarkus-cache 1.0.0-SNAPSHOT on JVM (powered by Quarkus 2.15.3.Final) started in 3.477s. Listening on: http://localhost:8080
2023-01-21 00:08:40,334 INFO  [io.quarkus] (Quarkus Main Thread) Profile dev activated. Live Coding activated.
2023-01-21 00:08:40,334 INFO  [io.quarkus] (Quarkus Main Thread) Installed features: [cache, cdi, config-yaml, micrometer, resteasy-reactive, scheduler, smallrye-context-propagation, vertx]
2023-01-21 00:08:41,017 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) refreshing
2023-01-21 00:08:41,027 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) invalidate
2023-01-21 00:08:41,030 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) calling expensive method
2023-01-21 00:08:43,030 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) expensive method called
2023-01-21 00:08:57,097 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:08:57,098 INFO  [de.tur.imp.CachedGreetingProvider] (executor-thread-0) calling expensive method
2023-01-21 00:08:59,098 INFO  [de.tur.imp.CachedGreetingProvider] (executor-thread-0) expensive method called
2023-01-21 00:09:00,090 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:00,524 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:00,992 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:00,999 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) refreshing
2023-01-21 00:09:01,000 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) invalidate
2023-01-21 00:09:01,000 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) calling expensive method
2023-01-21 00:09:01,326 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:03,001 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) expensive method called
2023-01-21 00:09:04,375 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:04,875 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
```

As we can see, the expensive is only called
- after application startup,
- after the cache has been invalidated (every 10 seconds), or
- when the cache is refreshed (every 30 seconds)

We also see in the logs that the caching mechanic of quarkus takes care of thread-safety for us. In these log lines:
```
...
2023-01-21 00:09:00,999 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) refreshing
2023-01-21 00:09:01,000 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) invalidate
2023-01-21 00:09:01,000 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) calling expensive method
2023-01-21 00:09:01,326 INFO  [de.tur.GreetingResource] (executor-thread-0) endpoint called
2023-01-21 00:09:03,001 INFO  [de.tur.imp.CachedGreetingProvider] (vert.x-worker-thread-0) expensive method called
...
```
we see that the endpoint was called during the regular refresh. The endpoint-invocation of the cached value was blocked until the regular refresh was completed, and it did not call the expensive method again.

## Metrics
To get cache metrics, we have to explicitly enable them on a per-cache basis. In our case, we have to set `quarkus.cache.caffeine."greeting".metrics.enabled=true`. Then, when we visit http://localhost:8080/q/metrics, we see the statistics of cache hits and cache misses:
```
cache_gets_total{cache="greeting",result="miss",} 2.0
cache_gets_total{cache="greeting",result="hit",} 5.0
...
cache_puts_total{cache="greeting",} 2.0
```

(Notice that the entries might appear in a different order)