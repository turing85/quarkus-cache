package de.turing85;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import java.time.Duration;
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
    LOGGER.info("endpoint called");
    return getGreeting() + " from RESTEasy Reactive";
  }

  @Startup
  @CacheResult(cacheName = "greeting")
  String getGreeting() throws InterruptedException {
    LOGGER.info("calling expensive method");
    Thread.sleep(Duration.ofSeconds(2).toMillis());
    LOGGER.info("expensive method called");
    return "Hello";
  }

  @CacheInvalidate(cacheName = "greeting")
  void invalidate() {
    LOGGER.info("invalidate");
  }

  @Scheduled(cron = "{quarkus.cache.caffeine.greeting.refresh-cron}")
  void refreshCache() throws InterruptedException {
    LOGGER.info("refreshing");
    invalidate();
    getGreeting();
  }
}