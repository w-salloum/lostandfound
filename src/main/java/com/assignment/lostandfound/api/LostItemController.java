package com.assignment.lostandfound.api;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.service.LostItemService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;

    public LostItemController(LostItemService lostItemService) {
        this.lostItemService = lostItemService;
    }

    @GetMapping
    public List<LostItemDto> getLostItems() {
        return this.lostItemService.getLostItems();
    }

    @PostMapping
    public void postLostItem() {
        LostItemDto lostItem = LostItemDto
                .builder()
                .place("location")
                .name("name")
                .quantity(1)
                .build();
         this.lostItemService.saveLostItem(lostItem);
    }
}
