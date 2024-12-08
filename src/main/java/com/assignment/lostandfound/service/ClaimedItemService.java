package com.assignment.lostandfound.service;

import com.assignment.lostandfound.dto.ClaimedItemDto;
import com.assignment.lostandfound.dto.UserDetailsDto;
import com.assignment.lostandfound.entity.ClaimedItem;
import com.assignment.lostandfound.exception.NotFoundException;
import com.assignment.lostandfound.repository.ClaimedItemRepository;
import com.assignment.lostandfound.repository.LostItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClaimedItemService {
    private final ClaimedItemRepository claimedItemRepository;
    private final LostItemRepository lostItemRepository;
    private final UserService userService;

    public ClaimedItemService(ClaimedItemRepository claimedItemRepository, LostItemRepository lostItemRepository, UserService userService) {
        this.claimedItemRepository = claimedItemRepository;
        this.lostItemRepository = lostItemRepository;
        this.userService = userService;
    }

    public List<ClaimedItemDto> getClaimedItems(long lostItemId) {
        // Validate the lost item exists
        lostItemRepository.findById(lostItemId)
                .orElseThrow(() -> new NotFoundException("Lost Item not found"));

        // Fetch claimed items for the lost item
        List<ClaimedItem> claimedItems = claimedItemRepository.findByLostItemId(lostItemId);

        // Fetch user details in a single call
        Map<Long, UserDetailsDto> userDetailsMap = userService
                .getUsersByIds(
                        claimedItems.stream()
                                .map(ClaimedItem::getUserId)
                                .distinct()
                                .toList()
                )
                .stream()
                .collect(Collectors.toMap(UserDetailsDto::getId, userDetailsDto -> userDetailsDto));

        // Build and return the result list
        return claimedItems.stream()
                .map(claimedItem -> {
                    UserDetailsDto userDetailsDto = userDetailsMap.get(claimedItem.getUserId());
                    return ClaimedItemDto.builder()
                            .lostItemId(lostItemId)
                            .quantity(claimedItem.getQuantity())
                           // .claimedAt(claimedItem.getClaimedAt().toString())
                            .claimedAt("claimedItem.getClaimedAt().toString()")
                            .claimedBy(userDetailsDto != null ? userDetailsDto.getName() : null)
                            .build();
                })
                .toList();
    }

}
