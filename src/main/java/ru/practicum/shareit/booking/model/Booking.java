package ru.practicum.shareit.booking.model;

import lombok.Data;
import ru.practicum.shareit.booking.status.BookingStatus;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
public class Booking {

    private Long id;

    private LocalDate start;

    private LocalDate end;

    private Item item;

    private User user;

    private BookingStatus status;
}
