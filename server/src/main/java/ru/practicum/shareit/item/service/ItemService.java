package ru.practicum.shareit.item.service;

import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;

import java.util.List;

public interface ItemService {
    ItemDtoResponse create(ItemDtoRequest itemDtoRequest, Long userId);

    CommentDtoResponse addComment(CommentDtoRequest dto, Long itemId, Long userId);

    ItemDtoResponse update(ItemDtoRequest itemDtoRequest, Long itemId, Long userId);

    ItemDtoResponse findById(Long itemId, Long userId);

    List<ItemDtoResponse> findByUserId(Long userId);

    List<ItemDtoResponse> search(String text);
}