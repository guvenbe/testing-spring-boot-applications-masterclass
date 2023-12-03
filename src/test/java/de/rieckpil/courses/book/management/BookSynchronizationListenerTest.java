package de.rieckpil.courses.book.management;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSynchronizationListenerTest {

//  private final static String VALID_ISBN = BookSynchronizationListenerTest.VALID_ISBN;
  public static final String VALID_ISBN = "1234567891234";

  @Mock
  private BookRepository bookRepository;

  @Mock
  private OpenLibraryApiClient openLibraryApiClient;

  @InjectMocks
  private BookSynchronizationListener cut;

  @Captor
  private ArgumentCaptor<Book> bookArgumentCaptor;

  @Test
  void shouldRejectBookWhenIsbnIsMalformed() {
    System.out.println(cut.toString());
    System.out.println(bookRepository.getClass().getName());
    BookSynchronization bookSynchronization = new BookSynchronization("42");
    cut.consumeBookUpdates(bookSynchronization);
    verifyNoInteractions(openLibraryApiClient, bookRepository);
  }

  @Test
  void shouldNotOverrideWhenBookAlreadyExists() {
    BookSynchronization bookSynchronization = new BookSynchronization(BookSynchronizationListenerTest.VALID_ISBN);
    when(bookRepository.findByIsbn(BookSynchronizationListenerTest.VALID_ISBN)).thenReturn(new Book());
    cut.consumeBookUpdates(bookSynchronization);
    verifyNoInteractions(openLibraryApiClient);
    verify(bookRepository, never()).save(any());

  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
    BookSynchronization bookSynchronization = new BookSynchronization(BookSynchronizationListenerTest.VALID_ISBN);
    when(bookRepository.findByIsbn(BookSynchronizationListenerTest.VALID_ISBN)).thenReturn(null);
    when(openLibraryApiClient.fetchMetadataForBook(BookSynchronizationListenerTest.VALID_ISBN)).thenThrow(new RuntimeException("Something went wrong"));
    assertThrows(RuntimeException.class, () -> cut.consumeBookUpdates(bookSynchronization));
    verify(bookRepository, never()).save(any());
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
    BookSynchronization bookSynchronization = new BookSynchronization(BookSynchronizationListenerTest.VALID_ISBN);
    when(bookRepository.findByIsbn(BookSynchronizationListenerTest.VALID_ISBN)).thenReturn(null);
    Book requestedBook = new Book();
    requestedBook.setTitle("Java Book");
    requestedBook.setIsbn(BookSynchronizationListenerTest.VALID_ISBN);
    when(openLibraryApiClient.fetchMetadataForBook(BookSynchronizationListenerTest.VALID_ISBN)).thenReturn(requestedBook);
    when(bookRepository.save(ArgumentMatchers.any())).then(invocation -> {
      Book methodArgument = invocation.getArgument(0);
      methodArgument.setId(1L);
      return methodArgument;
    });
    cut.consumeBookUpdates(bookSynchronization);
    verify(bookRepository).save(bookArgumentCaptor.capture());
    assertEquals("Java Book", bookArgumentCaptor.getValue().getTitle());
    assertEquals(1L, bookArgumentCaptor.getValue().getId());
    assertEquals(BookSynchronizationListenerTest.VALID_ISBN, bookArgumentCaptor.getValue().getIsbn());
  }

}
