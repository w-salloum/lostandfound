package com.assignment.lostandfound.mapper;

import com.assignment.lostandfound.dto.LostItemDto;
import com.assignment.lostandfound.entity.LostItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LostItemMapper {

    @Mapping(target = "name", source = "item.name")
    @Mapping(target = "place", source = "place.name")
    LostItemDto toLostItemDto(LostItem lostItem);

    List<LostItemDto> toLostItemDtoList(List<LostItem> lostItems);
}
