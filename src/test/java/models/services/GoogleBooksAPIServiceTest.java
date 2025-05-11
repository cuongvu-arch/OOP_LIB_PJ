package models.services;

import models.entities.Document;
import models.services.GoogleBooksAPIService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GoogleBooksAPIServiceTest {

    @Test
    void testFetchBookInfo_validIsbn_returnsDocument() throws Exception {
        // Một ISBN thật: Clean Code
        String isbn = "9780132350884";

        Document doc = GoogleBooksAPIService.fetchBookInfo(isbn);

        assertNotNull(doc);
        assertEquals(isbn, doc.getIsbn());
        assertNotNull(doc.getTitle());
        assertNotNull(doc.getAuthors());
        assertTrue(doc.getAuthors().length > 0);
        assertNotNull(doc.getGoogleBooksUrl());
    }

    @Test
    void testFetchBookInfo_invalidIsbn_returnsNull() throws Exception {
        String isbn = "invalid-isbn-000000";

        Document doc = GoogleBooksAPIService.fetchBookInfo(isbn);

        assertNull(doc);
    }
}

