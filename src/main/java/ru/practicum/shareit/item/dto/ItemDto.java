package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;

import java.util.List;

@Data
public class ItemDto {

    private Long id;

    @NotBlank
    String name;

    @NotBlank
    String description;

    @NotNull
    Boolean available;

    BookingDtoResponse lastBooking;

    BookingDtoResponse nextBooking;

    List<CommentDtoResponse> comments;

}
