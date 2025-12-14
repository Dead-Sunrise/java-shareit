package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.client.ItemRequestClient;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody CreateItemRequestDto createItemRequestDto,
                                         @RequestHeader("X-Sharer-User-Id") Long userId) {
        if (createItemRequestDto.getDescription() == null || createItemRequestDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса должно быть указано");
        }
        return itemRequestClient.create(createItemRequestDto, userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAllRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestClient.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllRequests() {
        return itemRequestClient.getAllRequests();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequestById(@PathVariable Long requestId) {
        return itemRequestClient.getRequestById(requestId);
    }
}
