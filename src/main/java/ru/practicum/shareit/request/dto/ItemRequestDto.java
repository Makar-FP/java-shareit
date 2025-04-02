package ru.practicum.shareit.request.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequestDto {

    private Long id;

    private User requestor;

    private String description;

    private LocalDateTime created;
}
