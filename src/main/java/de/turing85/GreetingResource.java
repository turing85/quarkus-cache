package de.turing85;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/hello")
public class GreetingResource {
  private static final Logger LOGGER = LoggerFactory.getLogger(GreetingResource.class);

  private final CachedGreetingProvider greetingProvider;

  public GreetingResource(CachedGreetingProvider greetingProvider) {
    this.greetingProvider = greetingProvider;
  }

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    LOGGER.info("endpoint called");
    return greetingProvider.getGreeting() + " from RESTEasy Reactive";
  }
}