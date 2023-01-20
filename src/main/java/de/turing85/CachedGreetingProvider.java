package de.turing85;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import java.time.Duration;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup
@Singleton
public final class CachedGreetingProvider implements GreetingProvider {
  private static final Logger LOGGER = LoggerFactory.getLogger(CachedGreetingProvider.class);

  CachedGreetingProvider() {
    preheatCache();
  }

  void preheatCache() {
    getGreeting();
  }

  @CacheResult(cacheName = "greeting")
  public String getGreeting() {
    LOGGER.info("calling expensive method");
    try {
      Thread.sleep(Duration.ofSeconds(2).toMillis());
    } catch (InterruptedException e) {
      LOGGER.error("Error", e);
      Thread.currentThread().interrupt();
    }
    LOGGER.info("expensive method called");
    return "Hello";
  }

  @CacheInvalidate(cacheName = "greeting")
  void invalidate() {
    LOGGER.info("invalidate");
  }

  @Scheduled(cron = "${cache.greeting.refresh-cron}")
  void refresh() {
    LOGGER.info("refreshing");
    invalidate();
    getGreeting();
  }
}
