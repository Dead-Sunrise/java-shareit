package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(CreateItemRequestDto itemRequestDto, Long userId);

    List<ItemRequestDto> getAllRequestsByUser(Long userId);

    List<ItemRequestDto> getAllRequests();

    ItemRequestDto getRequestById(Long requestId);
}
