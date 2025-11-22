package ru.practicum.shareit.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.request.ItemRequest;

@Component
public class ItemRequestMapper {
    public ItemRequestDto itemRequestToDto(ItemRequest itemRequest) {
        if (itemRequest == null) {
            return null;
        }
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .user(itemRequest.getUser())
                .created(itemRequest.getCreated())
                .build();
    }

    public ItemRequest dtoToItemRequest(ItemRequestDto itemRequestDto) {
        if (itemRequestDto == null) {
            return null;
        }
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .user(itemRequestDto.getUser())
                .created(itemRequestDto.getCreated())
                .build();
    }
}
