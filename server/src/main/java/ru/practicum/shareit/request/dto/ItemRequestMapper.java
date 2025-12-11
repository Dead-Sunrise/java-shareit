package ru.practicum.shareit.request.dto;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserShortDto;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ItemRequestMapper {
    public ItemRequestDto itemRequestToDto(ItemRequest itemRequest, List<ItemShortDto> items) {
        if (itemRequest == null) {
            return null;
        }
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .user(new UserShortDto(itemRequest.getUser().getId(), itemRequest.getUser().getName()))
                .created(itemRequest.getCreated())
                .items(items)
                .build();
    }

    public ItemRequest dtoToItemRequest(ItemRequestDto itemRequestDto, User user) {
        if (itemRequestDto == null) {
            return null;
        }
        return ItemRequest.builder()
                .id(itemRequestDto.getId())
                .description(itemRequestDto.getDescription())
                .user(user)
                .build();
    }

    public ItemRequest CreateRequestDtoToItemRequest(CreateItemRequestDto dto, User user) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setCreated(LocalDateTime.now());
        itemRequest.setDescription(dto.getDescription());
        itemRequest.setUser(user);
        return itemRequest;
    }
}
