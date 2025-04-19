package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.model.enums.BookingState;

import java.util.List;

public interface BookingService {

    BookingDtoResponse create(BookingDtoRequest booking, Long userId);

    BookingDtoResponse update(Long bookingId, Boolean approved, Long userId);

    BookingDtoResponse findById(Long userId, Long bookingId);

    List<BookingDtoResponse> getUserBookingsByState(Long userId, BookingState state);

    List<BookingDtoResponse> getUserItemsBookingsByState(Long userId, BookingState state);
}