package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookings;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping
    public List<ItemDtoWithCommentsAndBookings> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("/items GET Запрос на получение списка предметов определенного пользователя.");
        return itemService.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ItemDtoWithCommentsAndBookings getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("/items/{itemId} GET Запрос на получение данных предмета по id");
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return itemService.search(text);
    }

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        log.info("/items POST Запрос на добавление нового предмета владельцем");
        return itemService.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@PathVariable Long itemId,
                          @RequestHeader("X-Sharer-User-Id") Long userId,
                          @RequestBody ItemDto itemDto) {
        log.info("/items/{itemId} PUT Запрос на обновление предмета владельцем");
        return itemService.update(itemDto, userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long itemId) {
        log.info("/items/{itemId} DELETE Запрос на удаление предмета");
        itemService.deleteItemById(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto postComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        return itemService.postComment(userId, itemId, commentDto);
    }
}
