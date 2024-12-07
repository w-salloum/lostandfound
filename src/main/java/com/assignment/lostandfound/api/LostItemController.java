package com.assignment.lostandfound.api;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.service.LostItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
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

    @PostMapping("/upload")
    public ResponseEntity<String> uploadPdf(@RequestParam("file") MultipartFile file) {
        int totalItems = this.lostItemService.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully. Total items: " + totalItems);
    }
}
