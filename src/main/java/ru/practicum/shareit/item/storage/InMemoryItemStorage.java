package ru.practicum.shareit.item.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class InMemoryItemStorage implements ItemStorage {

    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 0L;

    @Override
    public Item create(Item item) {
        item.setId(genNextId());
        items.put(item.getId(), item);
        return item;
    }

    @Override
    public Item update(Item item) {
        if (!items.containsKey(item.getId())) {
            throw new NotFoundException("Item was not found");
        }

        Item existingItem = items.get(item.getId());
        updateItemFields(existingItem, item);

        return existingItem;
    }


    @Override
    public Optional<Item> findById(Long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    @Override
    public List<Item> findAllByUser(User user) {
        return items.values().stream()
                .filter(item -> item.getOwner().equals(user))
                .collect(Collectors.toList());
    }

    @Override
    public List<Item> findByQuery(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        String searchQuery = text.toLowerCase();
        List<Item> result = items.values().stream()
                .filter(item -> item.getAvailable() && matchesQuery(item, searchQuery))
                .collect(Collectors.toList());

        return result;
    }

    private boolean matchesQuery(Item item, String query) {
        return (item.getName() != null && item.getName().toLowerCase().contains(query))
                || (item.getDescription() != null && item.getDescription().toLowerCase().contains(query));
    }

    private Long genNextId() {
        return nextId++;
    }

    private void updateItemFields(Item existingItem, Item newItem) {
        Optional.ofNullable(newItem.getName()).ifPresent(existingItem::setName);
        Optional.ofNullable(newItem.getDescription()).ifPresent(existingItem::setDescription);
        Optional.ofNullable(newItem.getAvailable()).ifPresent(existingItem::setAvailable);
    }
}
