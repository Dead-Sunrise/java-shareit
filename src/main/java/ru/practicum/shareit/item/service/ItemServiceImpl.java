package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final ItemMapper itemMapper;

    @Override
    public List<ItemDto> getAllUserItems(Long userId) {
        userService.getUserById(userId);
        return itemRepository.getAllUserItems(userId).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto getItemById(Long id) {
        return itemMapper.itemToDto(itemRepository.getItemById(id));
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.search(text).stream()
                .map(itemMapper::itemToDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название предмета должно быть указано");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание предмета должно быть указано");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности предемета должен быть указан");
        }
        User user = userRepository.getUserById(userId);
        Item item = itemMapper.dtoToItem(itemDto, user);
        return itemMapper.itemToDto(itemRepository.create(item));
    }

    @Override
    public ItemDto update(ItemDto newItemDto, Long userId, Long itemId) {
        Item existingItem = itemRepository.getItemById(itemId);
        userService.getUserById(userId);
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new ValidationException("Запрос на редактирование предмета может создавать только владелец.");
        }
        if (newItemDto.getName() != null) {
            existingItem.setName(newItemDto.getName());
        }
        if (newItemDto.getDescription() != null) {
            existingItem.setDescription(newItemDto.getDescription());
        }
        if (newItemDto.getAvailable() != null) {
            existingItem.setAvailable(newItemDto.getAvailable());
        }
        return itemMapper.itemToDto(itemRepository.update(existingItem));
    }

    @Override
    public void deleteItemById(Long id) {
        itemRepository.deleteItemById(id);
    }
}
