package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> getAllUserItems(Long userId);

    ItemDto getItemById(Long id);

    List<ItemDto> search(String text);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto newItemDto, Long userId, Long itemId);

    void deleteItemById(Long id);
}
