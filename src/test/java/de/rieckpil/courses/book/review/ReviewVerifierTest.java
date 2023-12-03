package de.rieckpil.courses.book.review;

import org.assertj.core.api.Assertions;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;


import java.util.List;

import static de.rieckpil.courses.book.review.RandomReviewParameterResolverExtension.RandomReview;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(RandomReviewParameterResolverExtension.class)
class ReviewVerifierTest {

  private ReviewVerifier reviewVerifier;

  @BeforeEach
  void setup() {
    reviewVerifier = new ReviewVerifier();
  }

  @Test
  @DisplayName("Should fail when review contains swear word")
  void shouldFailWhenReviewContainsSwearWord() {
    String review = "This book is shit! Don't buy it and save your time and money Or give me your money";
    System.out.println("Testing a review");

    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "ReviewVerifier did not detect swear word");
  }

  @Test
  @DisplayName("Should fail when review contains 'lorem ipsum'")
  void testLoremIpsum() {
    String review = "Lorem ipsum this a test. Don't buy it and save your time and money Or give me your money";
    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result, "The text does not contain Lorem ipsum");
  }

  @ParameterizedTest
  @CsvFileSource(resources = "/badReview.csv")
  void shouldFailWhenReviewIsOfBadQuality(String review, Boolean expectedResult) {
    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertEquals(expectedResult,result,"Review verifier did not detect bad reviews");
    //assertFalse(result,"Review verifier did not detect bad reviews");
  }
  @RepeatedTest(5)
  void shouldFailWhenRandomReviewQualityIsBad(@RandomReview String review) {

    System.out.println(review);
    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertFalse(result,"Review verifier did not detect bad reviews");
  }

  @Test
  void shouldPassWhenReviewIsGood() {

    String review = "This book is great! I love it and I can recommend it to everyone" +
      "who is interested in learning about testing";

    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    assertTrue(result, "Review verifier did not detect good review");
  }

  @Test
  void shouldPassWhenReviewIsGoodHamcrest() {
    String review = "This book is great! I love it and I can recommend it to everyone" +
      "who is interested in learning about testing";

    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    //assertTrue(result, "Review verifier did not detect good review");
    assertThat("Review verifier did not detect good review",result, is(true));
    assertThat("Review verifier did not detect good review",result, equalTo(true));
    assertThat("Lorem ipsum", endsWith("ipsum"));
    assertThat(List.of(1,2,3,4,5), hasSize(5));
    assertThat(List.of(1,2,3,4,5), anyOf(hasSize(5), emptyIterable()));
//    assertThat(List.of(1,2,3,4,5), allOf(hasSize(5), emptyIterable()));
  }

  @Test
  void shouldPassWhenReviewIsGoodAssertJ() {
    String review = "This book is great! I love it and I can recommend it to everyone" +
      "who is interested in learning about testing";

    boolean result = reviewVerifier.doesMeetQualityStandards(review);
    //assertTrue(result, "Review verifier did not detect good review");
    Assertions.assertThat(result)
      .withFailMessage("Review verifier did not detect good review")
      .isTrue();
    Assertions.assertThat(result).isTrue();
    Assertions.assertThat(result)
      .withFailMessage("Review verifier did not detect good review")
      .isEqualTo(true);
    Assertions.assertThat("Lorem ipsum").endsWith("ipsum");
    Assertions.assertThat(List.of(1,2,3,4,5)).contains(3).isNotEmpty();
    Assertions.assertThat(List.of(1,2,3,4,5)).hasSize(5);
    Assertions.assertThat(List.of(1,2,3,4,5)).hasSizeBetween(0, 10);

  }
}
