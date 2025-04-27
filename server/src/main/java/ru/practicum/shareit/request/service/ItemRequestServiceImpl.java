package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final UserStorage userStorage;
    private final ItemRequestStorage itemRequestStorage;
    private final ItemStorage itemStorage;

    @Override
    @Transactional
    public ItemRequestDtoResponse create(ItemRequestDtoRequest dto, Long userId) {
        User user = getUserOrThrow(userId);
        ItemRequest request = itemRequestStorage.save(ItemRequestMapper.toEntity(dto, user));
        return ItemRequestMapper.toDto(request);
    }

    @Override
    public List<ItemRequestDtoResponse> findUserRequests(Long userId) {
        validateUserExists(userId);
        List<ItemRequest> requests = getRequestsByUser(userId);
        List<Item> items = getItemsForRequests(requests);
        return ItemRequestMapper.toDto(requests, items);
    }

    @Override
    public List<ItemRequestDtoResponse> findAllRequests(Long userId) {
        List<ItemRequest> requests = itemRequestStorage.findByOwnerIdNotOrderByCreatedDesc(userId);
        return ItemRequestMapper.toDto(requests);
    }

    @Override
    public ItemRequestDtoResponse findRequestById(Long requestId, Long userId) {
        validateUserExists(userId);
        ItemRequest request = getRequestOrThrow(requestId);
        List<Item> items = itemStorage.findAllByRequestId(List.of(requestId));
        return ItemRequestMapper.toDto(request, items);
    }

    private User getUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " was not found"));
    }

    private void validateUserExists(Long userId) {
        if (!userStorage.existsById(userId)) {
            throw new NotFoundException("User with ID " + userId + " was not found");
        }
    }

    private ItemRequest getRequestOrThrow(Long requestId) {
        return itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Request with ID " + requestId + " was not found"));
    }

    private List<ItemRequest> getRequestsByUser(Long userId) {
        return itemRequestStorage.findAllByOwnerIdOrderByCreatedDesc(userId);
    }

    private List<Item> getItemsForRequests(List<ItemRequest> requests) {
        List<Long> requestIds = requests.stream().map(ItemRequest::getId).toList();
        return itemStorage.findAllByRequestId(requestIds);
    }
}
