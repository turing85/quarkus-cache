package de.turing85.impl;

import de.turing85.ValuesProvider;
import io.agroal.api.AgroalDataSource;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.runtime.Startup;
import io.quarkus.scheduler.Scheduled;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@Startup
@ApplicationScoped
@Slf4j
public final class CachedValuesProvider implements ValuesProvider {
  public static final String CACHE_NAME = "values";

  private final AgroalDataSource dataSource;

  CachedValuesProvider(
      @SuppressWarnings("CdiInjectionPointsInspection") AgroalDataSource dataSource) {
    this.dataSource = dataSource;
  }

  @PostConstruct
  void preheatCache() {
    getValues();
  }

  @CacheResult(cacheName = CACHE_NAME)
  @Override
  public Set<Long> getValues() {
    log.info("calling database");
    Set<Long> result = fetchValuesFromDatabase();
    log.info("database called");
    return result;
  }

  private Set<Long> fetchValuesFromDatabase() {
    Set<Long> result = new HashSet<>();
    try (
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT id FROM public.data")) {
      while (rs.next()) {
        result.add(rs.getLong("id"));
      }
    } catch (SQLException e) {
      log.error("Error during data fetching", e);
    }
    return result;
  }

  @CacheInvalidate(cacheName = CACHE_NAME)
  void invalidate() {
    log.info("invalidating cache \"{}\"", CACHE_NAME);
  }

  @Scheduled(cron = "${cache.greeting.refresh-cron}")
  void refresh() {
    log.info("refreshing cache \"{}\"", CACHE_NAME);
    invalidate();
    getValues();
  }
}
