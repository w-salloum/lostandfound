package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.ClaimedItem;
import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.entity.LostItem;
import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.exception.InvalidRequestException;
import com.assignment.lostandfound.exception.NotFoundException;
import com.assignment.lostandfound.exception.UnsupportedFileTypeException;
import com.assignment.lostandfound.mapper.LostItemMapper;
import com.assignment.lostandfound.reader.FileReader;
import com.assignment.lostandfound.reader.PDFFileReader;
import com.assignment.lostandfound.repository.ClaimedItemRepository;
import com.assignment.lostandfound.repository.LostItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LostItemService {

    private static final String ITEM_NOT_FOUND_MSG = "Lost item not found with id: ";
    private static final String USER_NOT_FOUND_MSG = "User not found with id: ";
    private static final String INVALID_QUANTITY_MSG = "Invalid quantity, it must be more than 0 and less than or equal to available quantity: ";
    private static final String INVALID_FILE_MSG = "Invalid file. Please upload a file.";
    private static final String UNSUPPORTED_FILE_TYPE_MSG = "Unsupported file type: ";

    private final ItemService itemService;
    private final PlaceService locationService;
    private final LostItemRepository lostItemRepository;
    private final LostItemMapper lostItemMapper;
    private final UserService userService;
    private final ClaimedItemRepository claimedItemRepository;

    public LostItemService(ItemService itemService, PlaceService locationService,
                           LostItemRepository lostItemRepository, LostItemMapper lostItemMapper,
                           UserService userService, ClaimedItemRepository claimedItemRepository) {
        this.itemService = itemService;
        this.locationService = locationService;
        this.lostItemRepository = lostItemRepository;
        this.lostItemMapper = lostItemMapper;
        this.userService = userService;
        this.claimedItemRepository = claimedItemRepository;
    }

    @Transactional
    public void saveLostItem(LostItemDto lostItemDto) {
        Item item = itemService.getOrSaveItem(lostItemDto.getName());
        Place place = locationService.getOrSaveLocation(lostItemDto.getPlace());

        lostItemRepository.findByItemIdAndPlaceId(item.getId(), place.getId())
                .ifPresentOrElse(
                        existingItem -> updateLostItemQuantity(existingItem, lostItemDto.getQuantity()),
                        () -> createNewLostItem(item, place, lostItemDto.getQuantity())
                );
    }

    private void updateLostItemQuantity(LostItem lostItem, int quantity) {
        lostItem.setQuantity(lostItem.getQuantity() + quantity);
        lostItemRepository.save(lostItem);
    }

    private void createNewLostItem(Item item, Place place, int quantity) {
        LostItem lostItem = new LostItem();
        lostItem.setItem(item);
        lostItem.setPlace(place);
        lostItem.setQuantity(quantity);
        lostItemRepository.save(lostItem);
    }

    public List<LostItemDto> getLostItems() {
        return lostItemMapper.toLostItemDtoList(lostItemRepository.findAll());
    }

    @Transactional
    public int uploadFile(MultipartFile file) {
        validateFile(file);
        try (var inputStream = file.getInputStream()) {
            // get the appropriate file reader based on the file type
            // then start processing the file
            return getFileReader(file.getOriginalFilename()).processFile(inputStream);
        } catch (IOException e) {
            throw new InvalidPDFContentException("Error reading file: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidPDFContentException(INVALID_FILE_MSG);
        }
    }

    public FileReader getFileReader(String filename) {
        if (filename.endsWith(".pdf")) {
            return new PDFFileReader(this);
        }
        throw new UnsupportedFileTypeException(UNSUPPORTED_FILE_TYPE_MSG + filename);
    }

    @Transactional
    public void claimLostItem(Long lostItemId, Long userId, int quantity) {
        // validation
        LostItem lostItem = lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND_MSG + lostItemId));

        if (!userService.isUserExist(userId)) {
            throw new NotFoundException(USER_NOT_FOUND_MSG + userId);
        }

        if (quantity <= 0 || quantity > lostItem.getQuantity() - lostItem.getClaimedQuantity()) {
            throw new InvalidRequestException(INVALID_QUANTITY_MSG + quantity);
        }

        // increment claimed quantity and save claimed item
        // used this field to keep track of claimed quantity, and prevent over-claiming
        lostItem.setClaimedQuantity(lostItem.getClaimedQuantity() + quantity);

        ClaimedItem claimedItem = new ClaimedItem();
        claimedItem.setLostItem(lostItem);
        claimedItem.setUserId(userId);
        claimedItem.setQuantity(quantity);
        claimedItem.setClaimedAt(LocalDateTime.now());

        lostItemRepository.save(lostItem);  // This save can be done once in the claim process
        claimedItemRepository.save(claimedItem);
    }
}
