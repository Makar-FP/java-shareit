package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoRequest;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.enums.BookingState;
import ru.practicum.shareit.booking.model.enums.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingServiceImpl implements BookingService {

    private final BookingStorage bookingStorage;
    private final UserStorage userStorage;
    private final ItemStorage itemStorage;

    @Override
    @Transactional
    public BookingDtoResponse create(BookingDtoRequest dto, Long userId) {
        User user = getUser(userId);
        Item item = getItem(dto.getItemId());

        validateItemAvailable(item);

        Booking booking = bookingStorage.save(BookingMapper.mapDtoToNewBooking(dto, user, item));
        return BookingMapper.mapBookingToDto(booking);
    }

    @Override
    public BookingDtoResponse update(Long bookingId, Boolean status, Long userId) {
        Booking booking = getBooking(bookingId);
        validateOwner(booking, userId);
        validateWaitingStatus(booking);

        booking.setStatus(status ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        bookingStorage.save(booking);

        return BookingMapper.mapBookingToDto(booking);
    }

    @Override
    public BookingDtoResponse findById(Long bookingId, Long userId) {
        validateUserExists(userId);
        Booking booking = getBooking(bookingId);
        return BookingMapper.mapBookingToDto(booking);
    }

    @Override
    public List<BookingDtoResponse> getUserBookingsByState(Long userId, BookingState state) {
        validateUserExists(userId);
        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingStorage.findAllByBookerIdAndEndAfterOrderByStartDesc(userId, now);
            case PAST -> bookingStorage.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingStorage.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingStorage.findAllByBookerId(userId);
        };

        return bookings.stream()
                .map(BookingMapper::mapBookingToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDtoResponse> getUserItemsBookingsByState(Long userId, BookingState state) {
        validateUserExists(userId);

        LocalDateTime now = LocalDateTime.now();
        List<Booking> result =  switch (state) {
            case CURRENT -> bookingStorage.findAllByBookerIdAndEndAfterOrderByStartDesc(userId, now);
            case PAST -> bookingStorage.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now);
            case FUTURE -> bookingStorage.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now);
            case WAITING -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookingStorage.findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookingStorage.findAllByBookerId(userId);
        };

        return BookingMapper.mapBookingToDto(result);
    }

    private User getUser(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " was not found"));
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with id " + userId + " was not found");
        }
    }

    private Item getItem(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with id " + itemId + " was not found"));
    }

    private void validateItemAvailable(Item item) {
        if (!item.getAvailable()) {
            throw new ValidationException("Item with id " + item.getId() + " is not available for booking");
        }
    }

    private Booking getBooking(Long bookingId) {
        return bookingStorage.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking with id " + bookingId + " was not found"));
    }

    private void validateOwner(Booking booking, Long userId) {
        Long ownerId = booking.getItem().getOwner().getId();
        if (!Objects.equals(ownerId, userId)) {
            throw new ValidationException("User with id " + userId + " is not the owner of the item with id " +
                    booking.getItem().getId());
        }
    }

    private void validateWaitingStatus(Booking booking) {
        if (!Objects.equals(booking.getStatus(), BookingStatus.WAITING)) {
            throw new ValidationException("Booking is already approved or rejected");
        }
    }
}