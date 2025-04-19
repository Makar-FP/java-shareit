package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {

    public static ItemDto mapItemToItemDto(Item item, BookingDtoResponse lastBooking, BookingDtoResponse nextBooking,
                                           List<CommentDtoResponse> comments) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static Item mapItemDtoToItem(ItemDto dto, User owner) {
        Item item = new Item();
        item.setOwner(owner);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        return item;
    }

    public static ItemDto mapItemToItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }

    public static List<ItemDto> mapItemToItemDto(List<Item> result) {
        List<ItemDto> dtoList = new ArrayList<>();
        for (Item item : result) {
            dtoList.add(mapItemToItemDto(item));
        }
        return dtoList;
    }
}
