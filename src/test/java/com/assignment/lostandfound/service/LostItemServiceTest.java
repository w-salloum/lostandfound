package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.entity.LostItem;
import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.exception.InvalidRequestException;
import com.assignment.lostandfound.exception.NotFoundException;
import com.assignment.lostandfound.exception.UnsupportedFileTypeException;
import com.assignment.lostandfound.mapper.LostItemMapper;
import com.assignment.lostandfound.repository.ClaimedItemRepository;
import com.assignment.lostandfound.repository.LostItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Optional;

import static com.assignment.lostandfound.util.FileUtils.getPdfInputStream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LostItemServiceTest {

    @Mock
    private ItemService itemService;

    @Mock
    private PlaceService locationService;

    @Mock
    private LostItemRepository lostItemRepository;

    @Mock
    private LostItemMapper lostItemMapper;

    @Mock
    private UserService userService;

    @Mock
    private ClaimedItemRepository claimedItemRepository;

    @InjectMocks
    private LostItemService lostItemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSaveLostItem_CreatesNewLostItem() {
        LostItemDto lostItemDto =  LostItemDto.builder()
                .name("item1")
                .place("place1")
                .quantity(5)
                .build();

        Item mockItem = new Item();
        mockItem.setId(1L);
        Place mockPlace = new Place();
        mockPlace.setId(1L);

        when(itemService.getOrSaveItem("item1")).thenReturn(mockItem);
        when(locationService.getOrSaveLocation("place1")).thenReturn(mockPlace);
        when(lostItemRepository.findByItemIdAndPlaceId(mockItem.getId(), mockPlace.getId())).thenReturn(Optional.empty());

        lostItemService.saveLostItem(lostItemDto);

        verify(lostItemRepository, times(1)).save(any(LostItem.class)); // Check if save is called once
    }

    @Test
    void testSaveLostItem_UpdatesExistingLostItem() {
        LostItemDto lostItemDto = LostItemDto.builder()
                .name("item1")
                .place("place1")
                .quantity(5)
                .build();

        Item mockItem = new Item();
        mockItem.setId(1L);
        Place mockPlace = new Place();
        mockPlace.setId(1L);
        LostItem existingLostItem = new LostItem();
        existingLostItem.setQuantity(10);

        when(itemService.getOrSaveItem("item1")).thenReturn(mockItem);
        when(locationService.getOrSaveLocation("place1")).thenReturn(mockPlace);
        when(lostItemRepository.findByItemIdAndPlaceId(mockItem.getId(), mockPlace.getId())).thenReturn(Optional.of(existingLostItem));

        lostItemService.saveLostItem(lostItemDto);

        verify(lostItemRepository, times(1)).save(existingLostItem); // Check if save is called once for updating
    }

    @Test
    void testClaimLostItem_ThrowsNotFoundException_WhenLostItemNotFound() {
        Long lostItemId = 1L;
        Long userId = 1L;
        int quantity = 2;

        when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            lostItemService.claimLostItem(lostItemId, userId, quantity);
        });

        assertEquals("Lost item not found with id: " + lostItemId, exception.getMessage());
    }

    @Test
    void testClaimLostItem_ThrowsInvalidRequestException_WhenQuantityInvalid() {
        Long lostItemId = 1L;
        Long userId = 1L;
        int quantity = 100;

        LostItem mockLostItem = new LostItem();
        mockLostItem.setId(lostItemId);
        mockLostItem.setQuantity(10); // Less than the quantity we're trying to claim

        when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(mockLostItem));
        when(userService.isUserExist(userId)).thenReturn(true);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            lostItemService.claimLostItem(lostItemId, userId, quantity);
        });

        assertEquals("Invalid quantity, it must be more than 0 and less than or equal to available quantity: " + quantity, exception.getMessage());
    }

    @Test
    void testClaimLostItem_Success() {
        Long lostItemId = 1L;
        Long userId = 1L;
        int quantity = 2;

        LostItem mockLostItem = new LostItem();
        mockLostItem.setId(lostItemId);
        mockLostItem.setQuantity(10);
        mockLostItem.setClaimedQuantity(3);

        when(lostItemRepository.findById(lostItemId)).thenReturn(Optional.of(mockLostItem));
        when(userService.isUserExist(userId)).thenReturn(true);

        lostItemService.claimLostItem(lostItemId, userId, quantity);

        verify(lostItemRepository, times(1)).save(mockLostItem); // Ensure lost item is saved (updated)
        verify(claimedItemRepository, times(1)).save(any()); // Ensure claimed item is saved
    }

    @Test
    void testUploadFile_ThrowsUnsupportedFileTypeException_WhenFileIsNotPdf() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("file.txt");

        UnsupportedFileTypeException exception = assertThrows(UnsupportedFileTypeException.class, () -> {
            lostItemService.uploadFile(file);
        });

        assertEquals("Unsupported file type: file.txt", exception.getMessage());
        verify(lostItemRepository, times(0)).save(any());

    }

    @Test
    void testUploadFile_ThrowsInvalidPDFContentException_WhenFileIsEmpty() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        InvalidPDFContentException exception = assertThrows(InvalidPDFContentException.class, () -> {
            lostItemService.uploadFile(file);
        });

        assertEquals("Invalid file. Please upload a file.", exception.getMessage());
        verify(lostItemRepository, times(0)).save(any());

    }

    @Test
    void testUploadFile_Success() throws IOException {
        MultipartFile file = new MockMultipartFile("file", "fileName.pdf", "application/pdf", getPdfInputStream("data/sample.pdf"));
        Item mockItem = createItem(1L);
        Place mockPlace = createPlace(1L);
        LostItem mockLostItem = createLostItem(1L, 5, 2, mockItem, mockPlace);

        when(itemService.getOrSaveItem(any())).thenReturn(mockItem);
        when(locationService.getOrSaveLocation(any())).thenReturn(mockPlace);
        when(lostItemRepository.findByItemIdAndPlaceId(any(), any())).thenReturn(Optional.of(mockLostItem));

        int result = lostItemService.uploadFile(file);

        assertEquals(4, result);
        verify(locationService, times(4)).getOrSaveLocation(any());
        verify(itemService, times(4)).getOrSaveItem(any());
        verify(lostItemRepository, times(4)).save(any());
    }

    private LostItem createLostItem(Long id, int quantity, int claimedQuantity, Item item, Place place) {
        LostItem lostItem = new LostItem();
        lostItem.setId(id);
        lostItem.setQuantity(quantity);
        lostItem.setClaimedQuantity(claimedQuantity);
        lostItem.setItem(item);
        lostItem.setPlace(place);
        return lostItem;
    }

    private Item createItem(Long id) {
        Item item = new Item();
        item.setId(id);
        return item;
    }

    private Place createPlace(Long id) {
        Place place = new Place();
        place.setId(id);
        return place;
    }
}
