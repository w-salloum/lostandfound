package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.entity.LostItem;
import com.assignment.lostandfound.entity.Place;
import com.assignment.lostandfound.mapper.LostItemMapper;
import com.assignment.lostandfound.repository.LostItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
}
