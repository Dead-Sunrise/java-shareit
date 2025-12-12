package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({ItemServiceImpl.class, ItemMapper.class, CommentMapper.class, BookingMapper.class})
public class ItemServiceImplTest {
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User user1, user2;
    private ItemDto itemDto1, itemDto2;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        user1 = userRepository.save(User.builder()
                .id(1L)
                .name("Name1")
                .email("user1@yandex.ru")
                .build());
        user2 = userRepository.save(User.builder()
                .id(1L)
                .name("Name2")
                .email("user2@yandex.ru")
                .build());
        itemDto1 = ItemDto.builder()
                .name("Item1")
                .description("Description1")
                .available(true)
                .ownerId(user1.getId())
                .build();
        itemDto2 = ItemDto.builder()
                .name("Item2")
                .description("Description2")
                .available(true)
                .ownerId(user1.getId())
                .build();
    }

    @Test
    void createNewItemTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        assertNotNull(createdItem.getId());
        assertEquals("Item1", createdItem.getName());
        assertEquals("Description1", createdItem.getDescription());
        assertEquals(user1.getId(), createdItem.getOwnerId());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void createItemWithEmptyNameTest() {
        ItemDto invalidItemDto = ItemDto.builder()
                .name("")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.create(invalidItemDto, user1.getId()));
        assertEquals("Название предмета должно быть указано", exception.getMessage());
    }

    @Test
    void createItemWithEmptyDescriptionTest() {
        ItemDto invalidItemDto = ItemDto.builder()
                .name("Item")
                .description("")
                .available(true)
                .ownerId(user1.getId())
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.create(invalidItemDto, user1.getId()));
        assertEquals("Описание предмета должно быть указано", exception.getMessage());
    }

    @Test
    void createItemWithNullAvailableTest() {
        ItemDto invalidItemDto = ItemDto.builder()
                .name("Item")
                .description("Description")
                .available(null)
                .ownerId(user1.getId())
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.create(invalidItemDto, user1.getId()));
        assertEquals("Статус доступности предемета должен быть указан", exception.getMessage());
    }

    @Test
    void createItemWithNonExistentUserTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.create(itemDto1, 999L));
        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void createItemWithRequestTest() {
        ItemRequest itemRequest = ItemRequest.builder()
                .description("Request description")
                .user(user2)
                .created(LocalDateTime.now())
                .build();
        itemRequest = itemRequestRepository.save(itemRequest);
        ItemDto itemDtoWithRequest = ItemDto.builder()
                .name("Item with request")
                .description("Description")
                .available(true)
                .ownerId(user1.getId())
                .requestId(itemRequest.getId())
                .build();
        ItemDto createdItem = itemService.create(itemDtoWithRequest, user1.getId());
        assertNotNull(createdItem);
        assertEquals(itemRequest.getId(), createdItem.getRequestId());
    }

    @Test
    void getItemByIdTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        ItemDtoWithCommentsAndBookings resultItem = itemService.getItemById(createdItem.getId(), user1.getId());
        assertNotNull(resultItem);
        assertEquals(createdItem.getId(), resultItem.getId());
        assertEquals(createdItem.getName(), resultItem.getName());
    }

    @Test
    void getItemByInvalidIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getItemById(999L, user1.getId()));
        assertEquals("Предмет с id 999 не найден.", exception.getMessage());
    }

    @Test
    void getItemByIdWithBookingsForOwnerTest() {
        Item item = itemRepository.save(Item.builder()
                .name("Item for booking")
                .description("Description")
                .available(true)
                .owner(user1)
                .build());
        Booking lastBooking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(item)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(lastBooking);
        Booking nextBooking = Booking.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(item)
                .booker(user2)
                .status(BookingStatus.WAITING)
                .build();
        bookingRepository.save(nextBooking);
        ItemDtoWithCommentsAndBookings result = itemService.getItemById(item.getId(), user1.getId());
        assertNotNull(result);
        assertNotNull(result.getLastBooking());
        assertNotNull(result.getNextBooking());
    }

    @Test
    void getItemByIdWithBookingsForNonOwnerTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        ItemDtoWithCommentsAndBookings result = itemService.getItemById(createdItem.getId(), user2.getId());
        assertNotNull(result);
        assertNull(result.getLastBooking());
        assertNull(result.getNextBooking());
    }

    @Test
    void getItemWithCommentsTest() {
        Item item = itemRepository.save(Item.builder()
                .name("Item with comments")
                .description("Description")
                .available(true)
                .owner(user1)
                .build());
        Comment comment = Comment.builder()
                .text("Comment")
                .author(user2)
                .item(item)
                .created(LocalDateTime.now())
                .build();
        commentRepository.save(comment);
        ItemDtoWithCommentsAndBookings result = itemService.getItemById(item.getId(), user1.getId());
        assertNotNull(result);
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Comment", result.getComments().get(0).getText());
    }

    @Test
    void getAllUserItemsTest() {
        itemRepository.deleteAll();
        itemService.create(itemDto1, user1.getId());
        itemService.create(itemDto2, user1.getId());
        List<ItemDtoWithCommentsAndBookings> items = itemService.getAllUserItems(user1.getId());
        assertEquals(2, items.size());
        List<String> names = items.stream()
                .map(ItemDtoWithCommentsAndBookings::getName)
                .toList();
        assertTrue(names.contains("Item1"));
        assertTrue(names.contains("Item2"));
    }

    @Test
    void getAllUserItemsByInvalidUserIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.getAllUserItems(999L));
        assertEquals("Пользователь не найден.", exception.getMessage());
    }

    @Test
    void updateItemTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        ItemDto updatedItem = itemService.update(itemDto2, user1.getId(), createdItem.getId());
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals("Item2", updatedItem.getName());
        assertEquals("Description2", updatedItem.getDescription());
    }

    @Test
    void updateItemByInvalidIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemDto2, user1.getId(), 999L));
        assertEquals("Предмет с id 999 не найден.", exception.getMessage());
    }

    @Test
    void updateItemByNonOwnerTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> itemService.update(itemDto2, user2.getId(), createdItem.getId()));
        assertEquals("Запрос на редактирование предмета может создавать только владелец.", exception.getMessage());
    }

    @Test
    void deleteItemTest() {
        ItemDto createdItem = itemService.create(itemDto1, user1.getId());
        itemService.deleteItemById(createdItem.getId());
        assertThrows(NotFoundException.class, () -> itemService.getItemById(createdItem.getId(), user1.getId()));
    }

    @Test
    void searchItemTest() {
        itemRepository.deleteAll();
        itemService.create(itemDto1, user1.getId());
        itemService.create(itemDto2, user1.getId());
        List<ItemDto> items = itemService.search("Item2");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals("Item2", items.get(0).getName());
    }

    @Test
    void searchWithEmptyTextTest() {
        itemService.create(itemDto1, user1.getId());
        List<ItemDto> items = itemService.search("");
        assertNotNull(items);
        assertTrue(items.isEmpty());
    }

    @Test
    void createCommentTest() {
        Item item = Item.builder()
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(user1)
                .build();
        Item savedItem = itemRepository.save(item);
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(savedItem)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);
        CommentDto commentDto = CommentDto.builder()
                .text("Comment")
                .build();
        CommentDto result = itemService.postComment(user2.getId(), savedItem.getId(), commentDto);
        assertNotNull(result);
        assertEquals("Comment", result.getText());
        assertEquals(user2.getName(), result.getAuthorName());
        assertTrue(commentRepository.findById(result.getId()).isPresent());
    }

    @Test
    void createCommentWithInvalidCommentatorTest() {
        Item item = itemRepository.save(Item.builder()
                .name("Item for comment")
                .description("Description")
                .available(true)
                .owner(user1)
                .build());
        CommentDto commentDto = CommentDto.builder()
                .text("Comment")
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.postComment(user2.getId(), item.getId(), commentDto));
        assertEquals("Комментатор не брал данный предмет в аренду, либо аренда ещё не завершена.", exception.getMessage());
    }

    @Test
    void createCommentWithInvalidItemStatusTest() {
        Item item = itemRepository.save(Item.builder()
                .name("Item for comment")
                .description("Description")
                .available(true)
                .owner(user1)
                .build());
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(1))
                .item(item)
                .booker(user2)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);
        CommentDto commentDto = CommentDto.builder()
                .text("Comment")
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> itemService.postComment(user2.getId(), item.getId(), commentDto));
        assertEquals("Комментатор не брал данный предмет в аренду, либо аренда ещё не завершена.", exception.getMessage());
    }
}