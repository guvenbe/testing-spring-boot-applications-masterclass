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


import static de.rieckpil.courses.book.review.RandomReviewParameterResolverExtension.RandomReview;
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
  }

  @Test
  void shouldPassWhenReviewIsGoodAssertJ() {
  }
}
