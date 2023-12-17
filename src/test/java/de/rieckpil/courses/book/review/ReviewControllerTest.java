package de.rieckpil.courses.book.review;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.rieckpil.courses.config.WebSecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
// see https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-2.7-Release-Notes#migrating-from-websecurityconfigureradapter-to-securityfilterchain
@Import(WebSecurityConfig.class)
class ReviewControllerTest {

  @MockBean
  private ReviewService reviewService;

  @Autowired
  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @BeforeEach
  void setup() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void shouldReturnTwentyReviewsWithoutAnyOrderWhenNoParametersAreSpecified() throws Exception {

    ArrayNode result = objectMapper.createArrayNode();
    ObjectNode statistic = objectMapper.createObjectNode();
    statistic.put("id", 1L);
    statistic.put("isbn", "42");
    statistic.put("avg", 89.3);
    statistic.put("ratings", 2);
    result.add(statistic);

    when(reviewService.getAllReviews(20, "none")).thenReturn(result);

    this.mockMvc.perform(get("/api/books/reviews")
      .header("Accept", MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaType.APPLICATION_JSON))
      .andExpect(jsonPath("$.length()", Matchers.is(1)))
      .andExpect(jsonPath("$[0].isbn", Matchers.is("42")))
      .andExpect(jsonPath("$[0].avg", Matchers.is(89.3)))
      .andExpect(jsonPath("$[0].ratings", Matchers.is(2)))
      .andDo(MockMvcResultHandlers.print())
      .andReturn();

    verify(reviewService, times(1)).getAllReviews(20, "none");
  }

  @Test
  void shouldNotReturnReviewStatisticsWhenUserIsUnauthenticated() throws Exception {
    this.mockMvc.perform(get("/api/books/reviews/statistics")
      .header("Accept", MediaType.APPLICATION_JSON_VALUE))
      .andExpect(status().isUnauthorized())
      .andDo(MockMvcResultHandlers.print());
    verifyNoInteractions(reviewService);
  }

  @Test
  //@WithMockUser(username = "duke")
  void shouldReturnReviewStatisticsWhenUserIsAuthenticated() throws Exception {
    this.mockMvc.perform(get("/api/books/reviews/statistics")
        .header("Accept", MediaType.APPLICATION_JSON_VALUE)
//      .with(user("duke")))
        .with(jwt())
//        .with(httpBasic("duke", "password")))
      )
      .andDo(MockMvcResultHandlers.print())
      .andExpect(status().isOk());
    verify(reviewService, times(1)).getReviewStatistics();


  }

  @Test
  void shouldCreateNewBookReviewForAuthenticatedUserWithValidPayload() throws Exception {
    String requesBody = """
      {
        "reviewTitle": "Great Java Book",
        "reviewContent": "I really like this book",
        "rating": 4
      }
      """;
    when(reviewService.createBookReview(eq("42"),
      any(BookReviewRequest.class),
      eq("duke"),
      endsWith("spring.oi")))
      .thenReturn(84L);

    this.mockMvc.perform(post("/api/books/{isbn}/reviews", 42)
      .contentType(MediaType.APPLICATION_JSON)
      .content(requesBody)
      .with(jwt().jwt(builder -> builder
        .claim("preferred_username", "duke").claim("email", "duke@spring.oi")))
    ).andExpect(status().isCreated())
      .andExpect(header().exists("Location"))
      .andExpect(header().string("Location", Matchers.containsString("books/42/reviews/84")))
      .andDo(MockMvcResultHandlers.print());

  }

  @Test
  void shouldRejectNewBookReviewForAuthenticatedUsersWithInvalidPayload() throws Exception {
    //Don't give review title and make rating negative
    String requesBody = """
      {
        "reviewContent": "I really like this book",
        "rating": -1
      }
      """;

    this.mockMvc.perform(post("/api/books/{isbn}/reviews", 42)
        .contentType(MediaType.APPLICATION_JSON)
        .content(requesBody)
        .with(jwt().jwt(builder -> builder
          .claim("preferred_username", "duke").claim("email", "duke@spring.oi")))
      ).andExpect(status().isBadRequest())
       .andDo(MockMvcResultHandlers.print());
  }

  @Test
  void shouldNotAllowDeletingReviewsWhenUserIsAuthenticatedWithoutModeratorRole() throws Exception {
    this.mockMvc.perform(delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
        .with(jwt()))
      .andExpect(status().isForbidden());

    verifyNoInteractions(reviewService);
  }

  @Test
  @WithMockUser(roles = "moderator")
  void shouldAllowDeletingReviewsWhenUserIsAuthenticatedAndHasModeratorRole() throws Exception {
    this.mockMvc.perform(delete("/api/books/{isbn}/reviews/{reviewId}", 42, 3)
//        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_moderator"))))
        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_moderator"))))
      .andExpect(status().isOk());

    verify(reviewService, times(1)).deleteReview("42", 3L);
  }
}
