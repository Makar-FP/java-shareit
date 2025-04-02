package ru.practicum.shareit.item.storage;

import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface ItemStorage {

    Item create(Item item);

    Item update(Item item);

    Optional<Item> findById(Long itemId);

    List<Item> findAllByUser(User user);

    List<Item> findByQuery(String text);
}
