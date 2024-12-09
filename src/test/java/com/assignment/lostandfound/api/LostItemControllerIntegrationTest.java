package com.assignment.lostandfound.api;

import com.assignment.lostandfound.repository.ClaimedItemRepository;
import com.assignment.lostandfound.repository.ItemRepository;
import com.assignment.lostandfound.repository.LostItemRepository;
import com.assignment.lostandfound.repository.PlaceRepository;
import com.assignment.lostandfound.service.ClaimedItemService;
import com.assignment.lostandfound.service.LostItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static com.assignment.lostandfound.util.FileUtils.getPdfInputStream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class LostItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LostItemService lostItemService;

    @Autowired
    private ClaimedItemService claimedItemService;
    @Autowired
    private LostItemRepository lostItemRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private PlaceRepository placeRepository;
    @Autowired
    private ClaimedItemRepository claimedItemRepository;

    @AfterEach
    void tearDown() {
        // Reset mock
        claimedItemRepository.deleteAll();
        lostItemRepository.deleteAll();
        itemRepository.deleteAll();
        placeRepository.deleteAll();

    }

    @Test
    void testUploadFileAndGetLostItems() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", // Form field name
                "fileName.pdf", // Original file name
                "application/pdf", // Content type
                getPdfInputStream("data/sample.pdf") // File content as InputStream
        );

        // Act & Assert
        mockMvc.perform(multipart("/api/lost-items/upload")
                        .file(file)) // Attach file
                .andExpect(status().isOk())
                .andExpect(content().string("File uploaded successfully. Total items: 4"));

        // Verify
        mockMvc.perform(get("/api/lost-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(4))
                .andExpect(jsonPath("$[0].name").value("Laptop"))
                .andExpect(jsonPath("$[0].place").value("Taxi"))
                .andExpect(jsonPath("$[0].quantity").value("1"));

    }

    @Test
    void testClaimLostItem_NotFoundException() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/lost-items/1/claim/1/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetClaimedItems_NotFoundException() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/lost-items/177/claimed-items")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

