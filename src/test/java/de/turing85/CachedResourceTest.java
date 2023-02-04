package de.turing85;

import static com.google.common.truth.Truth.assertThat;
import static io.restassured.RestAssured.when;

import de.turing85.impl.CachedValuesProvider;
import io.quarkus.cache.Cache;
import io.quarkus.cache.CacheName;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
@TestHTTPEndpoint(CachedResource.class)
class CachedResourceTest {
  @SuppressWarnings("CdiInjectionPointsInspection")
  @Inject
  DataSource dataSource;

  @Inject
  @CacheName(CachedValuesProvider.CACHE_NAME)
  Cache cache;

  @Test
  void emptyResponse() throws SQLException {
    // GIVEN
    dbExecute("TRUNCATE TABLE public.data");

    // WHEN
    List<Long> firstActual = when().get()

    // THEN
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract().body().as(new TypeRef<>() {});
    assertThat(firstActual).isEmpty();

    // WHEN
    List<Long> secondActual = when().get()

    // THEN
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract().body().as(new TypeRef<>() {});
    assertThat(secondActual).isEmpty();
  }

  @Test
  void nonEmptyResponse() throws SQLException {
    // GIVEN
    dbExecute("TRUNCATE TABLE public.data");
    Set<Long> expected = Set.of(0L, 1L, 2L, 3L);
    for (long value : expected) {
      dbExecute("INSERT INTO public.data(id) VALUES(%d);".formatted(value));
    }
    cache.invalidateAll().await().atMost(Duration.ofSeconds(5L));

    // WHEN
    Set<Long> actual = when().get()

    // THEN
        .then()
            .statusCode(Response.Status.OK.getStatusCode())
            .extract().body().as(new TypeRef<>() {});
    assertThat(actual).isEqualTo(expected);
  }

  private void dbExecute(String query) throws SQLException {
    try (
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.execute(query);
    }
  }
}