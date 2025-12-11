package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookings;

import java.util.List;

public interface ItemService {
    List<ItemDtoWithCommentsAndBookings> getAllUserItems(Long userId);

    ItemDtoWithCommentsAndBookings getItemById(Long itemId, Long userId);

    List<ItemDto> search(String text);

    ItemDto create(ItemDto itemDto, Long userId);

    ItemDto update(ItemDto newItemDto, Long userId, Long itemId);

    void deleteItemById(Long id);

    CommentDto postComment(Long userId, Long itemId, CommentDto commentDto);
}
