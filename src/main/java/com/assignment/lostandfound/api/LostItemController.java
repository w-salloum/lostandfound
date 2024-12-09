package com.assignment.lostandfound.api;

import com.assignment.lostandfound.dto.ClaimedItemDto;
import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.service.ClaimedItemService;
import com.assignment.lostandfound.service.LostItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/lost-items")
public class LostItemController {

    private final LostItemService lostItemService;
    private final ClaimedItemService claimedItemService;

    public LostItemController(LostItemService lostItemService, ClaimedItemService claimedItemService) {
        this.lostItemService = lostItemService;
        this.claimedItemService = claimedItemService;
    }

    @GetMapping
    public ResponseEntity<List<LostItemDto>> getLostItems() {
        List<LostItemDto> lostItems = this.lostItemService.getLostItems();
        return ResponseEntity.ok(lostItems);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        int totalItems = this.lostItemService.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully. Total items: " + totalItems);
    }

    @PostMapping("/{lostItemId}/claim/{userId}/{quantity}")
    public ResponseEntity<String> claimLostItem(@PathVariable Long lostItemId, @PathVariable Long userId, @PathVariable int quantity) {
        this.lostItemService.claimLostItem(lostItemId, userId, quantity);
        return ResponseEntity.ok("Claim successful for user " + userId + " with quantity " + quantity);
    }

    @GetMapping("/{lostItemId}/claimed-items")
    public ResponseEntity<List<ClaimedItemDto>> getClaimedItems(@PathVariable Long lostItemId) {
        List<ClaimedItemDto> claimedItems = this.claimedItemService.getClaimedItems(lostItemId);
        return ResponseEntity.ok(claimedItems);
    }
}
