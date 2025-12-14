package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.client.ItemClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.validator.ItemValidator;

@Controller
@RequestMapping(path = "/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemClient itemClient;
    private final ItemValidator itemValidator;

    @GetMapping
    public ResponseEntity<Object> getAllUserItems(@RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("/items GET Запрос на получение списка предметов определенного пользователя.");
        return itemClient.getAllUserItems(userId);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable Long itemId, @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("/items/{itemId} GET Запрос на получение данных предмета по id");
        return itemClient.getItemById(itemId, userId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam String text) {
        return itemClient.search(text);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestHeader("X-Sharer-User-Id") Long userId, @RequestBody ItemDto itemDto) {
        itemValidator.itemValidate(itemDto);
        return itemClient.create(itemDto, userId);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> update(@PathVariable Long itemId,
                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @RequestBody ItemDto itemDto) {
        log.info("/items/{itemId} PUT Запрос на обновление предмета владельцем");
        itemValidator.itemValidate(itemDto);
        return itemClient.update(itemDto, userId, itemId);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> delete(@PathVariable Long itemId) {
        log.info("/items/{itemId} DELETE Запрос на удаление предмета");
        return itemClient.deleteItemById(itemId);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> postComment(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @PathVariable Long itemId,
            @RequestBody CommentDto commentDto) {
        itemValidator.commentValidate(commentDto);
        return itemClient.postComment(userId, itemId, commentDto);
    }
}
