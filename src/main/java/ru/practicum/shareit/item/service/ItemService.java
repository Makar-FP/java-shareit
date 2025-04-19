package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto create(ItemDto itemDto, Long userId);

    CommentDtoResponse addComment(CommentDtoRequest dto, Long itemId, Long userId);

    ItemDto update(ItemDto itemDto, Long itemId, Long userId);

    ItemDto findById(Long itemId, Long userId);

    List<ItemDto> findByUserId(Long userId);

    List<ItemDto> search(String text);
}