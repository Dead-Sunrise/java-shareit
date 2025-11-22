package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

public interface ItemRepository {
    Collection<Item> getAllUserItems(Long userId);

    Item getItemById(Long id);

    Collection<Item> search(String text);

    Item create(Item item);

    Item update(Item newItem);

    void deleteItemById(Long id);
}
