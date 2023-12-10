package de.rieckpil.courses.book.management;

import de.rieckpil.courses.config.WebSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class BookControllerTest {

  @MockBean
  private BookManagementService bookManagementService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void shouldStart() {

  }

  @Test
  void shouldGetEmptyArrayWhenNoBooksExists() throws Exception {
    MvcResult mvcResult =
      this.mockMvc.perform(get("/api/books")
        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()", is(0)))
        .andDo(print())
        .andReturn();


  }

  @Test
  void shouldNotReturnXML() throws Exception {
    this.mockMvc.perform(get("/api/books")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE))
      .andExpect(status().isNotAcceptable())
      .andDo(print());
  }

  @Test
  void shouldGetBooksWhenServiceReturnsBooks() throws Exception {
    when(bookManagementService.getAllBooks()).thenReturn(List.of(
      createBook(1L, "1234567890", "Java 11", "Duke", "Java 11", "Java", 100L, "Spring", "https://www.oreilly.com/library/cover/9780596527754/250w/"),
      createBook(2L, "1234567891", "Java 12", "Duke", "Java 12", "Java", 100L, "Spring", "https://www.oreilly.com/library/cover/9780596527754/250w/"),
      createBook(3L, "1234567892", "Java 13", "Duke", "Java 13", "Java", 100L, "Spring", "https://www.oreilly.com/library/cover/9780596527754/250w/")
    ));

    this.mockMvc.perform(get("/api/books")
      .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.size()", is(3)))
      .andExpect(jsonPath("$[0].isbn", is("1234567890")))
      .andExpect(jsonPath("$[0].title", is("Java 11")))
      .andExpect(jsonPath("$[0].author", is("Duke")))
      .andExpect(jsonPath("$[0].description", is("Java 11")))
      .andExpect(jsonPath("$[0].genre", is("Java")))
      .andExpect(jsonPath("$[0].pages", is(100)))
      .andExpect(jsonPath("$[0].publisher", is("Spring")))
      .andExpect(jsonPath("$[0].thumbnailUrl", is("https://www.oreilly.com/library/cover/9780596527754/250w/")))
      .andExpect(jsonPath("$[1].isbn", is("1234567891")))
      .andExpect(jsonPath("$[1].title", is("Java 12")))
      .andExpect(jsonPath("$[1].author", is("Duke")))
      .andExpect(jsonPath("$[1].description", is("Java 12")))
      .andExpect(jsonPath("$[1].genre", is("Java")))
      .andExpect(jsonPath("$[1].pages", is(100)))
      .andExpect(jsonPath("$[1].publisher", is("Spring")))
      .andExpect(jsonPath("$[1].thumbnailUrl", is("https://www.oreilly.com/library/cover/9780596527754/250w/")))
      .andDo(print());
  }

  private Book createBook(Long id, String isbn, String title, String author, String description, String genre, Long pages, String publisher, String thumbnailUrl) {
    Book result = new Book();
    result.setId(id);
    result.setIsbn(isbn);
    result.setTitle(title);
    result.setAuthor(author);
    result.setDescription(description);
    result.setGenre(genre);
    result.setPages(pages);
    result.setPublisher(publisher);
    result.setThumbnailUrl(thumbnailUrl);
    return result;
  }

}
