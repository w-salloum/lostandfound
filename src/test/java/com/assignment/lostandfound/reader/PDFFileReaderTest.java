package com.assignment.lostandfound.reader;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.service.LostItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.assignment.lostandfound.util.FileUtils.getPdfInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PDFFileReaderTest {

    @Mock
    private LostItemService lostItemService;

    private PDFFileReader pdfFileReader;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        pdfFileReader = new PDFFileReader(lostItemService);
    }

    @Test
    void testProcessFile_success() throws Exception {

        doNothing().when(lostItemService).saveLostItem(any(LostItemDto.class));

        // Execute the method
        int totalItems = pdfFileReader.processFile(getPdfInputStream("data/sample.pdf"));

        // Verify that the LostItemService.saveLostItem was called with the correct item
        verify(lostItemService, times(4)).saveLostItem(any(LostItemDto.class));
        assertEquals(4, totalItems); // One item should be processed
    }

    @Test
    void testProcessFile_invalidPdfFormat() {

        // Execute the method and expect an InvalidPDFContentException
        assertThrows(InvalidPDFContentException.class, () -> {
            pdfFileReader.processFile(getPdfInputStream("data/invalid-sample.pdf"));
        });
    }



}
