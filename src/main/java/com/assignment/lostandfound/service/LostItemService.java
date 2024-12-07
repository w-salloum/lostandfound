package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.entity.LostItem;
import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.exception.InvalidPDFContentException;
import com.assignment.lostandfound.exception.UnsupportedFileTypeException;
import com.assignment.lostandfound.mapper.LostItemMapper;
import com.assignment.lostandfound.reader.FileReader;
import com.assignment.lostandfound.reader.PDFFileReader;
import com.assignment.lostandfound.repository.LostItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class LostItemService {

    private final ItemService itemService;
    private final PlaceService locationService;
    private final LostItemRepository lostItemRepository;
    private final LostItemMapper lostItemMapper;

    public LostItemService(ItemService itemService, PlaceService locationService,
                           LostItemRepository lostItemRepository, LostItemMapper lostItemMapper) {
        this.itemService = itemService;
        this.locationService = locationService;
        this.lostItemRepository = lostItemRepository;
        this.lostItemMapper = lostItemMapper;
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
}
