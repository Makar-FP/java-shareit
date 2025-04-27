package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
public class ItemRequestDtoRequest {

    @NotBlank
    @Size(max = 255, message = "Request must contain less than 255 characters")
    String description;

}

