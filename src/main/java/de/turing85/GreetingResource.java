package de.turing85;

import io.quarkus.cache.CacheResult;
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

  @CacheResult(cacheName = "greeting")
  String getGreeting() throws InterruptedException {
    LOGGER.info("expensive method called");
    Thread.sleep(Duration.ofSeconds(2).toMillis());
    return "Hello";
  }
}