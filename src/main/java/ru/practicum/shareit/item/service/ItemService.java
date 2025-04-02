package ru.practicum.shareit.item.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserStorage;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemService {
    private final ItemStorage itemStorage;
    private final UserStorage userStorage;

    public ItemService(ItemStorage itemStorage, UserStorage userStorage) {
        this.itemStorage = itemStorage;
        this.userStorage = userStorage;
    }

    public ItemDto create(ItemDto itemDto, Long userId) {
        if (itemDto.getName() == null || itemDto.getName().trim().isEmpty()) {
            throw new ValidationException("Item name cannot be empty");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().trim().isEmpty()) {
            throw new ValidationException("Item description cannot be empty");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Item availability must be specified");
        }
        User owner = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);

        return ItemMapper.toItemDto(itemStorage.create(item));
    }

    public ItemDto update(ItemDto itemDto, Long itemId, Long userId) {
        Item existingItem = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID " + itemId + " not found"));

        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("User with ID " + userId + " is not the owner of this item");
        }

        Item updatedItem = ItemMapper.toItem(itemDto);
        updatedItem.setId(itemId);
        updatedItem.setOwner(existingItem.getOwner());

        return ItemMapper.toItemDto(itemStorage.update(updatedItem));
    }

    public ItemDto getById(Long itemId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Item with ID " + itemId + " not found"));

        return ItemMapper.toItemDto(item);
    }

    public List<ItemDto> getAllByUser(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with ID " + userId + " not found"));

        return itemStorage.findAllByUser(user).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    public List<ItemDto> search(String text) {
        List<ItemDto> result = itemStorage.findByQuery(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());

        return result;
    }
}

