package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    @Override
    public ItemRequestDto create(CreateItemRequestDto createItemRequestDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        ItemRequest itemRequest = itemRequestMapper.CreateRequestDtoToItemRequest(createItemRequestDto, user);
        ItemRequest savedRequest = itemRequestRepository.save(itemRequest);
        List<ItemShortDto> items = itemRepository.findAllByRequestId(savedRequest.getId()).stream()
                .map(itemMapper::itemToItemShortDto)
                .collect(Collectors.toList());

        return itemRequestMapper.itemRequestToDto(savedRequest, items);
    }

    @Override
    public List<ItemRequestDto> getAllRequestsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        List<ItemRequest> requests = itemRequestRepository.findAllByRequesterId(userId);
        List<Long> requestsId = requests.stream().map(ItemRequest::getId).toList();
        Map<Long, List<ItemShortDto>> requestItems = itemRepository.findAllByRequestIdIn(requestsId).stream()
                .map(itemMapper::itemToItemShortDto)
                .collect(Collectors.groupingBy(ItemShortDto::getRequestId));

        return requests.stream()
                .map(itemRequest -> itemRequestMapper.itemRequestToDto(itemRequest, requestItems.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests() {

        List<ItemRequest> requests = itemRequestRepository.findAllByOrderByCreatedDesc();

        List<Long> requestsId = requests.stream()
                .map(ItemRequest::getId)
                .toList();

        Map<Long, List<ItemShortDto>> requestItems = itemRepository.findAllByRequestIdIn(requestsId).stream()
                .map(itemMapper::itemToItemShortDto)
                .collect(Collectors.groupingBy(ItemShortDto::getRequestId));

        return requests.stream()
                .map(itemRequest -> itemRequestMapper.itemRequestToDto(itemRequest, requestItems.get(itemRequest.getId())))
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long requestId) {
        ItemRequest itemRequest = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос по id не найден."));
        List<ItemShortDto> items = itemRepository.findAllByRequestId(itemRequest.getId()).stream()
                .map(itemMapper::itemToItemShortDto)
                .toList();
        return itemRequestMapper.itemRequestToDto(itemRequest, items);
    }
}
