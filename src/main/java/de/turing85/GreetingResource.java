package de.turing85;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.scheduler.Scheduled;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hello")
public class GreetingResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(GreetingResource.class);

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() throws InterruptedException {
    LOGGER.info("endpoit called");
    return getGreeting() + " from RESTEasy Reactive";
  }

  @CacheResult(cacheName = "greeting")
  String getGreeting() throws InterruptedException {
    LOGGER.info("expensive method called");
    Thread.sleep(TimeUnit.SECONDS.toMillis(2));
    return "Hello";
  }

  @Scheduled(
      identity = "invalidate-greeting-cache",
      every = "${cache.greeting.invalidate-every}",
      delayed = "${cache.greeting.invalidate-every}")
  @CacheInvalidate(cacheName = "greeting")
  void invalidate() {
    LOGGER.info("cache invalidated");
  }
}