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

    /**
     * Fetches a list of all lost items.
     *
     * @return List of lost items as {@code List<LostItemDto>}.
     *         HTTP 200 OK if successful.
     */
    @GetMapping
    public ResponseEntity<List<LostItemDto>> getLostItems() {
        List<LostItemDto> lostItems = this.lostItemService.getLostItems();
        return ResponseEntity.ok(lostItems);
    }

    /**
     * Uploads a file containing lost item details. The file must be in PDF format.
     *
     * @param file the file to upload as {@code MultipartFile}.
     * @return A success message with the total number of items processed.
     *         HTTP 200 OK if the file is uploaded successfully.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        int totalItems = this.lostItemService.uploadFile(file);
        return ResponseEntity.ok("File uploaded successfully. Total items: " + totalItems);
    }

    /**
     * Claims a specified quantity of a lost item for a user.
     *
     * @param lostItemId the ID of the lost item to claim.
     * @param userId the ID of the user claiming the item.
     * @param quantity the quantity of the item to claim.
     * @return A success message with details of the claim.
     *         HTTP 200 OK if the claim is successful.
     */
    @PostMapping("/{lostItemId}/claim/{userId}/{quantity}")
    public ResponseEntity<String> claimLostItem(@PathVariable Long lostItemId, @PathVariable Long userId, @PathVariable int quantity) {
        this.lostItemService.claimLostItem(lostItemId, userId, quantity);
        return ResponseEntity.ok("Claim successful for user " + userId + " with quantity " + quantity);
    }

    /**
     * Fetches a list of claimed items for a specific lost item.
     *
     * @param lostItemId the ID of the lost item whose claimed items are to be fetched.
     * @return List of claimed items as {@code List<ClaimedItemDto>}.
     *         HTTP 200 OK if successful.
     */
    @GetMapping("/{lostItemId}/claimed-items")
    public ResponseEntity<List<ClaimedItemDto>> getClaimedItems(@PathVariable Long lostItemId) {
        List<ClaimedItemDto> claimedItems = this.claimedItemService.getClaimedItems(lostItemId);
        return ResponseEntity.ok(claimedItems);
    }
}
