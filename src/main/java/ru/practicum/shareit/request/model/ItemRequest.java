package ru.practicum.shareit.request.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

@Data
@Builder
public class ItemRequest {

    private Long id;

    private User requestor;

    private String description;

    private LocalDateTime created;
}
