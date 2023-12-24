package de.rieckpil.courses.book.management;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClientConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class OpenLibraryApiClientTest {

  private MockWebServer mockWebServer;
  private OpenLibraryApiClient cut;

  private static final String ISBN = "9780596004651";
  protected static String VALID_RESPONSE;
  static {
    try {
      VALID_RESPONSE = new String(OpenLibraryApiClientTest.class
        .getClassLoader()
        .getResourceAsStream("stubs/openlibrary/success-" + ISBN + ".json")
        .readAllBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }



  @BeforeEach
  void setUp() throws IOException {
    HttpClient httpClient = HttpClient.create()
      .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2_000)
      .doOnConnected(connection ->
        connection.addHandlerLast(new ReadTimeoutHandler(2))
          .addHandlerLast(new WriteTimeoutHandler(2)));

    this.mockWebServer = new MockWebServer();
    this.mockWebServer.start();

   this.cut = new OpenLibraryApiClient(
     WebClient.builder()
      .clientConnector(new ReactorClientHttpConnector(httpClient))
      .baseUrl(this.mockWebServer.url("/").toString())
      .build());


  }
  @Test
  void notNull() {
    assertNotNull(cut);
    assertNotNull(mockWebServer);
  }

  @Test
  void shouldReturnBookWhenResultIsSuccess() throws InterruptedException {
    MockResponse mockResponse = new MockResponse()
      .addHeader("Content-Type", "application/json charset=utf-8")
      .setBody(VALID_RESPONSE);

    this.mockWebServer.enqueue(mockResponse);

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
  }

  @Test
  void shouldReturnBookWhenResultIsSuccessButLackingAllInformation() {
  }

  @Test
  void shouldPropagateExceptionWhenRemoteSystemIsDown() {
  }

  @Test
  void shouldRetryWhenRemoteSystemIsSlowOrFailing() {
  }
}
