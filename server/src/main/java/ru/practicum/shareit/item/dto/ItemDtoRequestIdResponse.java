package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemDtoRequestIdResponse {

    Long id;

    String name;

    String description;

    Long ownerId;

    Long requestId;
}