package de.rieckpil.courses.book.management;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;

import static org.junit.jupiter.api.Assertions.*;



@RestClientTest(OpenLibraryRestTemplateApiClient.class)
class OpenLibraryRestTemplateApiClientTest {

  @Autowired
  private OpenLibraryRestTemplateApiClient cut;

  @Autowired
  private MockRestServiceServer mockRestServiceServer;

  private static final String ISBN = "9780596004651";

  @Test
  void shouldInjectBeans() {
    assertNotNull(cut);
    assertNotNull(mockRestServiceServer);
  }

  @Test
  void shouldReturnBookWhenResultIsSuccess() {
    this.mockRestServiceServer
//      .expect(MockRestRequestMatchers.requestTo("/api/books?jscmd=data&format=json&bibkeys=" + ISBN))
      .expect(MockRestRequestMatchers.requestTo(Matchers.containsString(ISBN)))
      .andRespond(
        MockRestResponseCreators.withSuccess(new ClassPathResource("/stubs/openlibrary/success-" + ISBN + ".json")
                ,MediaType.APPLICATION_JSON));
    Book result = cut.fetchMetadataForBook(ISBN);
    assertEquals(ISBN, result.getIsbn());
    assertEquals("Head first Java", result.getTitle());
    assertEquals("https://covers.openlibrary.org/b/id/388761-S.jpg", result.getThumbnailUrl());
    assertEquals("Kathy Sierra", result.getAuthor());
    assertEquals("O'Reilly", result.getPublisher());
    assertEquals(619, result.getPages());
    assertEquals("Your brain on Java--a learner's guide--Cover.Includes index.", result.getDescription());
    assertEquals("Java (Computer program language)", result.getGenre());
    assertNull(result.getId());

    assertNotNull(result);

  }

  @Test
  void shouldReturnBookWhenResultIsSuccessButLackingAllInformation() {
  }

  @Test
  void shouldPropagateExceptionWhenRemoteSystemIsDown() {
  }

  @Test
  void shouldContainCorrectHeadersWhenRemoteSystemIsInvoked() {
  }
}
