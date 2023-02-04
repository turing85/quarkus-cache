package de.turing85;

import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Path("cache")
@Produces(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor
@Slf4j
public class CachedResource {
  private final ValuesProvider valueProvider;

  @GET
  public Set<Long> getValues() {
    log.info("endpoint called");
    return valueProvider.getValues();
  }
}