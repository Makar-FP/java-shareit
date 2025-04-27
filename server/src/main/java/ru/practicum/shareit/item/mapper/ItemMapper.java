package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequestIdResponse;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

public class ItemMapper {

    public static Item mapDtoToItem(ItemDtoRequest dto, User owner, ItemRequest request) {
        Item item = new Item();
        item.setOwner(owner);
        item.setName(dto.getName());
        item.setDescription(dto.getDescription());
        item.setAvailable(dto.getAvailable());
        item.setRequest(request);
        return item;
    }

    public static ItemDtoResponse mapItemToDto(Item item) {
        ItemDtoResponse dto = new ItemDtoResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        return dto;
    }

    public static ItemDtoResponse mapItemToDto(Item item, BookingDtoResponse lastBooking, BookingDtoResponse nextBooking,
                                               List<CommentDtoResponse> comments) {
        ItemDtoResponse dto = new ItemDtoResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setAvailable(item.getAvailable());
        dto.setLastBooking(lastBooking);
        dto.setNextBooking(nextBooking);
        dto.setComments(comments);
        return dto;
    }

    public static ItemDtoRequestIdResponse mapItemToDtoRequest(Item item) {
        ItemDtoRequestIdResponse dto = new ItemDtoRequestIdResponse();
        dto.setId(item.getId());
        dto.setName(item.getName());
        dto.setDescription(item.getDescription());
        dto.setOwnerId(item.getOwner().getId());
        return dto;
    }

    public static List<ItemDtoRequestIdResponse> mapItemToDtoRequest(Iterable<Item> items) {
        List<ItemDtoRequestIdResponse> dtoList = new ArrayList<>();
        for (Item item : items) {
            dtoList.add(mapItemToDtoRequest(item));
        }
        return dtoList;
    }
}