package models.services;

import models.dao.BorrowRecordDAO;
import models.entities.BorrowRecord;
import models.entities.BorrowedBookInfo;
import models.services.BorrowRecordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.Connection;
import java.sql.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BorrowRecordServiceTest {

    private BorrowRecordService service;

    @BeforeEach
    void setUp() {
        service = new BorrowRecordService();
    }

    @Test
    void testGetUnreturnedBookInfo_shouldReturnOnlyUnreturned() throws Exception {
        BorrowRecord returned = new BorrowRecord();
        returned.setReturnDate(new Date(System.currentTimeMillis()));

        BorrowRecord unreturned = new BorrowRecord(); // returnDate = null

        BorrowedBookInfo info1 = mock(BorrowedBookInfo.class);
        when(info1.getBorrowRecord()).thenReturn(returned);

        BorrowedBookInfo info2 = mock(BorrowedBookInfo.class);
        when(info2.getBorrowRecord()).thenReturn(unreturned);

        try (MockedConstruction<BorrowRecordDAO> mockDAO = mockConstruction(BorrowRecordDAO.class,
                (mock, context) -> when(mock.getBorrowedBooksWithInfoByUserId(any(), eq(1)))
                        .thenReturn(List.of(info1, info2)))) {

            List<BorrowedBookInfo> result = service.getUnreturnedBookInfo(mock(Connection.class), 1);
            assertEquals(1, result.size());
            assertSame(unreturned, result.get(0).getBorrowRecord());
        }
    }

    @Test
    void testGetReturnedBookInfo_shouldReturnOnlyReturned() throws Exception {
        BorrowRecord returned = new BorrowRecord();
        returned.setReturnDate(new Date(System.currentTimeMillis()));

        BorrowRecord unreturned = new BorrowRecord();

        BorrowedBookInfo info1 = mock(BorrowedBookInfo.class);
        when(info1.getBorrowRecord()).thenReturn(returned);

        BorrowedBookInfo info2 = mock(BorrowedBookInfo.class);
        when(info2.getBorrowRecord()).thenReturn(unreturned);

        try (MockedConstruction<BorrowRecordDAO> mockDAO = mockConstruction(BorrowRecordDAO.class,
                (mock, context) -> when(mock.getBorrowedBooksWithInfoByUserId(any(), eq(2)))
                        .thenReturn(List.of(info1, info2)))) {

            List<BorrowedBookInfo> result = service.getReturnedBookInfo(mock(Connection.class), 2);
            assertEquals(1, result.size());
            assertSame(returned, result.get(0).getBorrowRecord());
        }
    }

    @Test
    void testGetBorrowedBooksByUserId_shouldReturnListWhenConnectionAvailable() {
        BorrowedBookInfo mockInfo = mock(BorrowedBookInfo.class);

        try (MockedConstruction<BorrowRecordDAO> mockDAO = mockConstruction(BorrowRecordDAO.class,
                (mock, context) -> when(mock.getBorrowedBooksWithInfoByUserId(any(), eq(5)))
                        .thenReturn(List.of(mockInfo)))) {

            List<BorrowedBookInfo> result = service.getBorrowedBooksByUserId(5);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testGetRemainingDays_shouldReturnCorrectRemainingOrOverdue() {
        BorrowRecord notOverdue = new BorrowRecord();
        notOverdue.setBorrowDate(new Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000L));
        String remaining = service.getRemainingDays(notOverdue);
        assertTrue(remaining.contains("ngày còn lại"));

        BorrowRecord overdue = new BorrowRecord();
        overdue.setBorrowDate(new Date(System.currentTimeMillis() - 40 * 24 * 60 * 60 * 1000L));
        String result = service.getRemainingDays(overdue);
        assertEquals("Hết hạn", result);
    }

    @Test
    void testCheckAndUpdateBorrowStatus_shouldUpdateIfOverdue() {
        BorrowRecord overdue = new BorrowRecord();
        overdue.setBorrowDate(new Date(System.currentTimeMillis() - 40 * 24 * 60 * 60 * 1000L));
        overdue.setStatus("Đang mượn");

        try (MockedConstruction<BorrowRecordDAO> mockDAO = mockConstruction(BorrowRecordDAO.class,
                (mock, context) -> {
                    when(mock.getAllBorrowRecords(any())).thenReturn(List.of(overdue));
                    doAnswer(inv -> {
                        BorrowRecord updated = inv.getArgument(1);
                        assertEquals("Quá hạn", updated.getStatus());
                        return null;
                    }).when(mock).updateBorrowRecord(any(), any());
                })) {
            service.checkAndUpdateBorrowStatus(); // assert logic inside mock
        }
    }
}

