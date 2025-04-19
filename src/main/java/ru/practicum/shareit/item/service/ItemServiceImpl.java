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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
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

    @Override
    @Transactional
    public ItemDto create(ItemDto itemDto, Long userId) {
        validateItemDto(itemDto);
        User owner = getUserOrThrow(userId);
        Item item = ItemMapper.mapItemDtoToItem(itemDto, owner);
        item.setOwner(owner);
        return ItemMapper.mapItemToItemDto(itemStorage.save(item), null, null, List.of());
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(CommentDtoRequest dto, Long itemId, Long userId) {
        User author = getUserOrThrow(userId);
        Item item = getItemOrThrow(itemId);
        List<Booking> bookings = bookingStorage.findAllByUserIdAndItemIdAndEndBeforeOrderByStartDesc(userId, itemId, LocalDateTime.now());

        if (bookings.isEmpty()) {
            throw new ValidationException("User " + userId + " has not rented item " + itemId);
        }

        Comment comment = commentStorage.save(CommentMapper.mapDtoToComment(dto, author, item));
        return CommentMapper.mapCommentToDto(comment);
    }

    @Override
    @Transactional
    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        Item item = getItemIfOwner(itemId, userId);
        updateItemFields(item, itemDto);
        Item updated = itemStorage.save(item);
        List<CommentDtoResponse> comments = getCommentsDto(itemId);
        return ItemMapper.mapItemToItemDto(updated, null, null, comments);
    }

    @Override
    public ItemDto findById(Long itemId, Long userId) {
        Item item = getItemOrThrow(itemId);
        List<CommentDtoResponse> comments = getCommentsDto(itemId);

        if (item.getOwner() != null && item.getOwner().getId().equals(userId)) {
            List<Booking> bookings = bookingStorage.findAllByItemId(itemId);
            LocalDateTime now = LocalDateTime.now();

            BookingDtoResponse last = getLastBookingDto(bookings, now);
            BookingDtoResponse next = getNextBookingDto(bookings, now);

            return ItemMapper.mapItemToItemDto(item, last, next, comments);
        }

        return ItemMapper.mapItemToItemDto(item, null, null, comments);
    }

    @Override
    public List<ItemDto> findByUserId(Long userId) {
        List<Item> items = getItemsByUserId(userId);
        Map<Long, List<Booking>> bookingsGroup = getBookingsGroupedByItemId(items);
        Map<Long, List<CommentDtoResponse>> commentsGroup = getCommentsGroupedByItemId(items);
        LocalDateTime now = LocalDateTime.now();

        return items.stream()
                .map(item -> mapToItemDto(item, bookingsGroup.get(item.getId()), commentsGroup.get(item.getId()), now))
                .toList();
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text.isBlank()) {
            return new ArrayList<>();
        }

        List<Item> items = itemStorage.findByNameContainingIgnoreCase(text);
        items.addAll(itemStorage.findByDescriptionContainingIgnoreCase(text));

        List<Item> result = items
                .stream()
                .filter(Item::getAvailable)
                .toList();

        return ItemMapper.mapItemToItemDto(result);
    }

    private void validateItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
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

    private void updateItemFields(Item item, ItemDto dto) {
        if (dto.getName() != null) item.setName(dto.getName());
        if (dto.getDescription() != null) item.setDescription(dto.getDescription());
        if (dto.getAvailable() != null) item.setAvailable(dto.getAvailable());
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

    private ItemDto mapToItemDto(Item item, List<Booking> bookings, List<CommentDtoResponse> comments, LocalDateTime now) {
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

        return ItemMapper.mapItemToItemDto(item, bookingLast, bookingNext, commentList);
    }
}


