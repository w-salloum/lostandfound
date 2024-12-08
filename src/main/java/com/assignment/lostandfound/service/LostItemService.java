package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.ClaimedItem;
import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.entity.LostItem;
import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.exception.InvalidRequestException;
import com.assignment.lostandfound.exception.UnsupportedFileTypeException;
import com.assignment.lostandfound.exception.NotFoundException;
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
    public void saveLostItem(LostItemDto lostItem) {

        Item item = this.itemService.getOrSaveItem(lostItem.getName());
        Place place = this.locationService.getOrSaveLocation(lostItem.getPlace());

        this.lostItemRepository.findByItemIdAndPlaceId(item.getId(), place.getId())
                .ifPresentOrElse(
                        lostItemEntity -> {
                            lostItemEntity.setQuantity(lostItemEntity.getQuantity() + lostItem.getQuantity());
                            this.lostItemRepository.save(lostItemEntity);
                        },
                        () -> {
                            LostItem lostItemEntity = new LostItem();
                            lostItemEntity.setItem(item);
                            lostItemEntity.setPlace(place);
                            lostItemEntity.setQuantity(lostItem.getQuantity());
                            this.lostItemRepository.save(lostItemEntity);
                        }
                );

    }

    public List<LostItemDto> getLostItems() {
        return lostItemMapper.toLostItemDtoList(this.lostItemRepository.findAll());
    }

    @Transactional
    public int uploadFile(MultipartFile file) {
        validateFile(file);
        FileReader fileReader = getFileReader(file.getOriginalFilename());

        try (var inputStream = file.getInputStream()) {
            return fileReader.processFile(inputStream);
        } catch (IOException e) {
            throw new InvalidPDFContentException("Error reading PDF: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty() ) {
            throw new InvalidPDFContentException("Invalid file. Please upload a file.");
        }
    }

    public FileReader getFileReader(String filename) {
        if (filename.endsWith(".pdf")) {
            return new PDFFileReader(this);
        }
        throw new UnsupportedFileTypeException("Unsupported file type: " + filename);
    }

    @Transactional
    public void claimLostItem(Long lostItemId, Long userId, int quantity) {
        LostItem lostItem = this.lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new NotFoundException("Lost item not found with id: " + lostItemId));
        if (!this.userService.isUserExist(userId)) {
            throw new NotFoundException("User not found with id: " + userId);
        }
        if (quantity <= 0) {
            throw new InvalidRequestException("Invalid quantity, it must be more than 0: " + quantity);
        }
        if (lostItem.getQuantity() < quantity) {
            throw new InvalidRequestException("Invalid quantity, it must be less than or equal to available quantity: " + quantity);
        }
        if (lostItem.getClaimedQuantity() + quantity > lostItem.getQuantity()) {
            throw new InvalidRequestException("Invalid quantity, it must be less than or equal to available quantity: " + quantity);
        }

        lostItem.setClaimedQuantity(lostItem.getClaimedQuantity() + quantity);

        ClaimedItem claimedItem = new ClaimedItem();
        claimedItem.setLostItem(lostItem);
        claimedItem.setUserId(userId);
        claimedItem.setQuantity(quantity);
        claimedItem.setClaimedAt(LocalDateTime.now());
        this.lostItemRepository.save(lostItem);
        this.claimedItemRepository.save(claimedItem);
    }
}
