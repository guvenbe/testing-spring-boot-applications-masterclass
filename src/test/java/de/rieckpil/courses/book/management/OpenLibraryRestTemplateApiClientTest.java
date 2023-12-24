package de.rieckpil.courses.book.management;


import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpServerErrorException;

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
    String response =
      """
     {
      "9780596004651": {
        "publishers": [
          {
            "name": "O'Reilly"
          }
        ],
        "title": "Head second Java",
        "authors": [
          {
            "url": "https://openlibrary.org/authors/OL1400543A/Kathy_Sierra",
            "name": "Kathy Sierra"
          }
        ],
        "number_of_pages": 42,
        "cover": {
          "small": "https://covers.openlibrary.org/b/id/388761-S.jpg",
          "large": "https://covers.openlibrary.org/b/id/388761-L.jpg",
          "medium": "https://covers.openlibrary.org/b/id/388761-M.jpg"
        }
       }
     }
    """;
    this.mockRestServiceServer
      .expect(MockRestRequestMatchers.requestTo(Matchers.containsString("/api/books")))
      .andRespond(
        MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON));

//    this.mockRestServiceServer
//      .expect(MockRestRequestMatchers.requestTo(Matchers.containsString("/duke/42")))
//      .andRespond(
//        MockRestResponseCreators.withSuccess(response, MediaType.APPLICATION_JSON));

    Book result = cut.fetchMetadataForBook(ISBN);

    assertEquals(ISBN, result.getIsbn());
    assertEquals("Head second Java", result.getTitle());
    assertEquals("https://covers.openlibrary.org/b/id/388761-S.jpg", result.getThumbnailUrl());
    assertEquals("Kathy Sierra", result.getAuthor());
    assertEquals("O'Reilly", result.getPublisher());
    assertEquals(42, result.getPages());
    assertEquals("n.A", result.getDescription());
    assertEquals("n.A", result.getGenre());
    assertNull(result.getId());



//    this.mockRestServiceServer.verify();

  }

  @Test
  void shouldPropagateExceptionWhenRemoteSystemIsDown() {
    assertThrows(HttpServerErrorException.class, () -> {
      this.mockRestServiceServer
        .expect(MockRestRequestMatchers.requestTo("/api/books?jscmd=data&format=json&bibkeys=" + ISBN))
        .andRespond(
//          MockRestResponseCreators.withServerError());
          MockRestResponseCreators.withStatus(HttpStatus.BAD_GATEWAY));
      cut.fetchMetadataForBook(ISBN);
    });
  }

  @Test
  void shouldContainCorrectHeadersWhenRemoteSystemIsInvoked() {
      this.mockRestServiceServer
//      .expect(MockRestRequestMatchers.requestTo("/api/books?jscmd=data&format=json&bibkeys=" + ISBN))
        .expect(MockRestRequestMatchers.requestTo(Matchers.containsString(ISBN)))
        .andExpect(MockRestRequestMatchers.header("X-Custom-Auth", "Duke42"))
        .andExpect(MockRestRequestMatchers.header("X-Customer-Id", "42"))
        .andRespond(
          MockRestResponseCreators.withSuccess(new ClassPathResource("/stubs/openlibrary/success-" + ISBN + ".json")
            , MediaType.APPLICATION_JSON));
      Book result = cut.fetchMetadataForBook(ISBN);
      assertNotNull(result);
    }

}
