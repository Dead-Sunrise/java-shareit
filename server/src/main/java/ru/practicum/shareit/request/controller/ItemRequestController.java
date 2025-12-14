package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto create(@RequestBody CreateItemRequestDto createItemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.create(createItemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getAllRequestsByUser(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return itemRequestService.getAllRequestsByUser(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllRequests() {
        return itemRequestService.getAllRequests();
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getRequestById(@PathVariable Long requestId) {
        return itemRequestService.getRequestById(requestId);
    }
}
