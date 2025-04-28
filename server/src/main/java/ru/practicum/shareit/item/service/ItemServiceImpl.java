package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDtoResponse;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingStorage;
import ru.practicum.shareit.comment.dto.CommentDtoRequest;
import ru.practicum.shareit.comment.dto.CommentDtoResponse;
import ru.practicum.shareit.comment.mapper.CommentMapper;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.storage.CommentStorage;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDtoRequest;
import ru.practicum.shareit.item.dto.ItemDtoResponse;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;
    private final BookingStorage bookingStorage;
    private final CommentStorage commentStorage;
    private final ItemRequestStorage requestStorage;

    @Override
    @Transactional
    public ItemDtoResponse create(ItemDtoRequest itemDtoRequest, Long userId) {
        validateItemDto(itemDtoRequest);
        User owner = getUserOrThrow(userId);

        ItemRequest request = null;
        if (itemDtoRequest.getRequestId() != null) {
            request = requestStorage.findById(itemDtoRequest.getRequestId()).orElse(null);
        }

        Item item = ItemMapper.mapDtoToItem(itemDtoRequest, owner, request);
        item.setOwner(owner);
        return ItemMapper.mapItemToDto(itemStorage.save(item));
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(CommentDtoRequest dto, Long itemId, Long userId) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);

        LocalDateTime now = LocalDateTime.now();

        List<Booking> bookings = bookingStorage.findAllByBookerIdAndItemIdAndEndBeforeOrderByStartDesc(userId, itemId, now);

        if (bookings.isEmpty()) {
            throw new ValidationException("User " + userId + " has not rented item " + itemId + LocalDateTime.now());
        }

        Comment comment = commentStorage.save(CommentMapper.mapDtoToComment(dto, author, item));
        return CommentMapper.mapCommentToDto(comment);
    }

    @Override
    @Transactional
    public ItemDtoResponse update(ItemDtoRequest itemDtoRequest, Long itemId, Long userId) {
        Item item = getItemIfOwner(itemId, userId);

        if (itemDtoRequest.getName() != null) {
            item.setName(itemDtoRequest.getName());
        }
        if (itemDtoRequest.getDescription() != null) {
            item.setDescription(itemDtoRequest.getDescription());
        }
        if (itemDtoRequest.getAvailable() != null) {
            item.setAvailable(itemDtoRequest.getAvailable());
        }

        return ItemMapper.mapItemToDto(itemStorage.save(item));
    }

    @Override
    public ItemDtoResponse findById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        List<CommentDtoResponse> comments = getCommentsDto(itemId);

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingStorage.findAllByItemId(itemId);
            LocalDateTime now = LocalDateTime.now();

            BookingDtoResponse last = getLastBookingDto(bookings, now);
            BookingDtoResponse next = getNextBookingDto(bookings, now);

            return ItemMapper.mapItemToDto(item, last, next, comments);
        }

        return ItemMapper.mapItemToDto(item, null, null, comments);
    }

    @Override
    public List<ItemDtoResponse> findByUserId(Long userId) {
        List<Item> items = getItemsByUserId(userId);
        Map<Long, List<Booking>> bookingsGroup = getBookingsGroupedByItemId(items);
        Map<Long, List<CommentDtoResponse>> commentsGroup = getCommentsGroupedByItemId(items);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> mapToItemDto(item, bookingsGroup.get(item.getId()), commentsGroup.get(item.getId()), now))
                .toList();
    }

    @Override
    public List<ItemDtoResponse> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemStorage.findByNameContainingIgnoreCase(text);
        items.addAll(itemStorage.findByDescriptionContainingIgnoreCase(text));

        return items.stream()
                .filter(Item::getAvailable)
                .map(ItemMapper::mapItemToDto)
                .toList();
    }

    private void validateItemDto(ItemDtoRequest itemDtoRequest) {
        if (itemDtoRequest.getName() == null || itemDtoRequest.getName().trim().isEmpty()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDtoRequest.getDescription() == null || itemDtoRequest.getDescription().trim().isEmpty()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDtoRequest.getAvailable() == null) {
            throw new ValidationException("Item availability must be specified");
        }
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));
    }

    private Item getItemOrThrow(Long itemId) {
        return itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID " + itemId + " not found"));
    }

    private Item getItemIfOwner(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User with ID " + userId + " is not the owner of this item");
        }
        return item;
    }

    private List<CommentDtoResponse> getCommentsDto(Long itemId) {
        return commentStorage.findAllByItemId(itemId).stream()
                .map(CommentMapper::mapCommentToDto)
                .collect(Collectors.toList());
    }

    private BookingDtoResponse getLastBookingDto(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isBefore(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::mapBookingToDto)
                .orElse(null);
    }

    private BookingDtoResponse getNextBookingDto(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::mapBookingToDto)
                .orElse(null);
    }

    private List<Item> getItemsByUserId(Long userId) {
        return itemStorage.findByOwnerId(userId);
    }

    private Map<Long, List<Booking>> getBookingsGroupedByItemId(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Booking> bookings = bookingStorage.findAllByItemIdOrderByStartDesc(itemIds);
        return bookings.stream().collect(Collectors.groupingBy(b -> b.getItem().getId()));
    }

    private Map<Long, List<CommentDtoResponse>> getCommentsGroupedByItemId(List<Item> items) {
        List<Long> itemIds = items.stream().map(Item::getId).toList();
        List<Comment> comments = commentStorage.findAllByItemId(itemIds);
        return comments.stream().collect(Collectors.groupingBy(
                c -> c.getItem().getId(),
                Collectors.mapping(CommentMapper::mapCommentToDto, Collectors.toList())
        ));
    }

    private ItemDtoResponse mapToItemDto(Item item, List<Booking> bookings, List<CommentDtoResponse> comments, LocalDateTime now) {
        List<Booking> bookingList = bookings != null ? bookings : Collections.emptyList();
        List<CommentDtoResponse> commentList = comments != null ? comments : Collections.emptyList();

        BookingDtoResponse bookingLast = bookingList.stream()
                .filter(b -> b.getEnd().isBefore(now))
                .max(Comparator.comparing(Booking::getEnd))
                .map(BookingMapper::mapBookingToDto)
                .orElse(null);

        BookingDtoResponse bookingNext = bookingList.stream()
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(BookingMapper::mapBookingToDto)
                .orElse(null);

        return ItemMapper.mapItemToDto(item, bookingLast, bookingNext, commentList);
    }
}


