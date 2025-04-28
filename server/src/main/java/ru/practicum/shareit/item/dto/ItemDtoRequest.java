package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemDtoRequest {

    @NotBlank
    String name;

    @NotNull
    String description;

    @NotNull
    Boolean available;

    Long requestId;
}
