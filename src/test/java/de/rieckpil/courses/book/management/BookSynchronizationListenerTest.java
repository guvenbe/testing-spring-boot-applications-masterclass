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

  private final static String VALID_ISBN = "1234567891234";

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
    BookSynchronization bookSynchronization = new BookSynchronization("1234567891234");
    when(bookRepository.findByIsbn("1234567891234")).thenReturn(new Book());
    cut.consumeBookUpdates(bookSynchronization);
    verifyNoInteractions(openLibraryApiClient);
    verify(bookRepository, never()).save(any());

  }

  @Test
  void shouldThrowExceptionWhenProcessingFails() {
  }

  @Test
  void shouldStoreBookWhenNewAndCorrectIsbn() {
  }

}
