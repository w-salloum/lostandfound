package com.assignment.lostandfound.service;

import com.assignment.lostandfound.entity.Item;
import com.assignment.lostandfound.repository.ItemRepository;
import org.springframework.stereotype.Service;

@Service
public class ItemService {
    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Item getOrSaveItem(String name) {
        return itemRepository.findByName(name).orElseGet(() -> itemRepository.save(new Item(name)));
    }
}
