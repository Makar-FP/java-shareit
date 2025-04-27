package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingDtoRequest {

    @NotNull
    Long itemId;

    @NotNull
    LocalDateTime start;

    @NotNull
    LocalDateTime end;
}
