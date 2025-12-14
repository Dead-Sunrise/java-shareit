package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;
    private final BookingMapper bookingMapper;

    @Override
    public List<ItemDtoWithCommentsAndBookings> getAllUserItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        return items.stream()
                .map(item -> mapUserItems(item, userId))
                .collect(Collectors.toList());
    }

    @Override
    public ItemDtoWithCommentsAndBookings getItemById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден."));
        List<CommentDto> comments = commentRepository.findAllByItemId(itemId).stream()
                .map(commentMapper::commentToDto)
                .collect(Collectors.toList());
        BookingDto lastBooking = null;
        BookingDto nextBooking = null;
        if (item.getOwner().getId().equals(userId)) {
            lastBooking = getLastBooking(itemId);
            nextBooking = getNextBooking(itemId);
        }
        return itemMapper.itemToDtoWithCommentsAndBookings(item, comments, lastBooking, nextBooking);
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        return itemRepository.searchItem(text).stream()
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemMapper.dtoToItem(itemDto, user);
        if (itemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос по id не найден."));
            item.setRequest(itemRequest);
        }
        return itemMapper.itemToDto(itemRepository.save(item));
    }

    @Override
    public ItemDto update(ItemDto newItemDto, Long userId, Long itemId) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет с id " + itemId + " не найден."));
        userRepository.findById(userId);
        if (!existingItem.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Запрос на редактирование предмета может создавать только владелец.");
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
        if (newItemDto.getRequestId() != null) {
            ItemRequest itemRequest = itemRequestRepository.findById(newItemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос по id не найден."));
            existingItem.setRequest(itemRequest);
        }
        return itemMapper.itemToDto(itemRepository.save(existingItem));
    }

    @Override
    public void deleteItemById(Long id) {
        itemRepository.deleteById(id);
    }

    @Override
    public CommentDto postComment(Long userId, Long itemId, CommentDto commentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден."));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Предмет не найден."));
        LocalDateTime localDateTime = LocalDateTime.now();
        if (!bookingRepository.existsByBookerIdAndItemIdAndStatusAndEndBefore(userId,
                itemId, BookingStatus.APPROVED, localDateTime)) {
            throw new ValidationException("Комментатор не брал данный предмет в аренду, либо аренда ещё не завершена.");
        }
        Comment comment = commentMapper.dtoToComment(commentDto, user, item);
        comment.setCreated(localDateTime);
        return commentMapper.commentToDto(commentRepository.save(comment));
    }

    public ItemDtoWithCommentsAndBookings mapUserItems(Item item, Long userId) {
        BookingDto lastBooking = getLastBooking(item.getId());
        BookingDto nextBooking = getNextBooking(item.getId());
        List<CommentDto> comments = commentRepository.findAllByItemId(item.getId()).stream()
                .map(commentMapper::commentToDto)
                .toList();
        return itemMapper.itemToDtoWithCommentsAndBookings(item, comments, lastBooking, nextBooking);
    }

    private BookingDto getLastBooking(Long itemId) {
        return bookingMapper.bookingToDto(bookingRepository.findFirstByItemIdAndEndBeforeOrderByEndDesc(itemId, LocalDateTime.now()));
    }

    private BookingDto getNextBooking(Long itemId) {
        return bookingMapper.bookingToDto(bookingRepository.findFirstByItemIdAndStartAfterOrderByStartAsc(itemId, LocalDateTime.now()));
    }
}
