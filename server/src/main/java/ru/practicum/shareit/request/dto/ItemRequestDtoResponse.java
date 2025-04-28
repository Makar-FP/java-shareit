package ru.practicum.shareit.request.dto;

import lombok.*;
import ru.practicum.shareit.item.dto.ItemDtoRequestIdResponse;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemRequestDtoResponse {
    Long id;
    String description;
    LocalDateTime created;
    List<ItemDtoRequestIdResponse> items;
}