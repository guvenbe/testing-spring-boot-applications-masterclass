package de.rieckpil.courses.book.management;

import com.nimbusds.jose.JOSEException;
import de.rieckpil.courses.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

import static org.awaitility.Awaitility.given;

class BookSynchronizationListenerRefactoredIT extends AbstractIntegrationTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private BookRepository bookRepository;

  @Test
  void shouldGetSuccessWhenClientIsAuthenticated() throws JOSEException {
    this.webTestClient
      .get()
      .uri("/api/books/reviews/statistics")
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + getSignedJWT())
      .exchange()
      .expectStatus().is2xxSuccessful();


  }

  @Test
  void shouldReturnBookFromAPIWhenApplicationConsumesNewSyncRequest() {
  }
}
