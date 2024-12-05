package com.assignment.lostandfound.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/lostitems")
public class LostItemController {

    @GetMapping
    public String getLostItems() {
        return "Lost Items";
    }
}
