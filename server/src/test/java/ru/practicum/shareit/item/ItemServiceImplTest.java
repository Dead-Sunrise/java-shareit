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
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.service.ItemServiceImpl;
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

    private User newUser1, newUser2;
    private ItemDto newItem1, newItem2;

    @BeforeEach
    void setUp() {
        newUser1 = userRepository.save(User.builder()
                .id(1L)
                .name("Name1")
                .email("user1@yandex.ru")
                .build());
        newUser2 = userRepository.save(User.builder()
                .id(1L)
                .name("Name2")
                .email("user2@yandex.ru")
                .build());
        newItem1 = ItemDto.builder()
                .name("Item1")
                .description("Description1")
                .available(true)
                .ownerId(newUser1.getId())
                .build();
        newItem2 = ItemDto.builder()
                .name("Item2")
                .description("Description2")
                .available(true)
                .ownerId(newUser1.getId())
                .build();
    }

    @Test
    void createNewItemTest() {
        ItemDto createdItem = itemService.create(newItem1, newUser1.getId());
        assertNotNull(createdItem.getId());
        assertEquals("Item1", createdItem.getName());
        assertEquals("Description1", createdItem.getDescription());
        assertEquals(newUser1.getId(), createdItem.getOwnerId());
        assertTrue(createdItem.getAvailable());
    }

    @Test
    void getItemByIdTest() {
        ItemDto createdItem = itemService.create(newItem1, newUser1.getId());
        ItemDtoWithCommentsAndBookings resultItem = itemService.getItemById(createdItem.getId(), newUser1.getId());
        assertNotNull(resultItem);
        assertEquals(createdItem.getId(), resultItem.getId());
        assertEquals(createdItem.getName(), resultItem.getName());
    }

    @Test
    void getAllUserItemsTest() {
        ItemDto createdItem1 = itemService.create(newItem1, newUser1.getId());
        ItemDto createdItem2 = itemService.create(newItem2, newUser1.getId());
        List<ItemDtoWithCommentsAndBookings> items = itemService.getAllUserItems(newUser1.getId());
        assertEquals(2, items.size());
        List<String> names = items.stream()
                .map(ItemDtoWithCommentsAndBookings::getName)
                .toList();
        assertTrue(names.contains("Item1"));
        assertTrue(names.contains("Item2"));
    }

    @Test
    void updateItemTest() {
        ItemDto createdItem = itemService.create(newItem1, newUser1.getId());
        ItemDto updatedItem = itemService.update(newItem2, newUser1.getId(), createdItem.getId());
        assertEquals(createdItem.getId(), updatedItem.getId());
        assertEquals("Item2", updatedItem.getName());
        assertEquals("Description2", updatedItem.getDescription());
    }

    @Test
    void deleteItemTest() {
        ItemDto createdItem = itemService.create(newItem1, newUser1.getId());
        itemService.deleteItemById(createdItem.getId());
        assertThrows(NotFoundException.class, () -> itemService.getItemById(createdItem.getId(), newUser1.getId()));
    }

    @Test
    void searchItemTest() {
        ItemDto createdItem1 = itemService.create(newItem1, newUser1.getId());
        ItemDto createdItem2 = itemService.create(newItem2, newUser1.getId());
        List<ItemDto> items = itemService.search("Item2");
        assertNotNull(items);
        assertEquals(1, items.size());
        assertTrue(items.contains(createdItem2));
    }

    @Test
    void createCommentTest() {
        Item item = Item.builder()
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(newUser1)
                .build();
        Item savedItem = itemRepository.save(item);
        Booking booking = Booking.builder()
                .start(LocalDateTime.now().minusDays(2))
                .end(LocalDateTime.now().minusDays(1))
                .item(savedItem)
                .booker(newUser2)
                .status(BookingStatus.APPROVED)
                .build();
        bookingRepository.save(booking);
        CommentDto commentDto = CommentDto.builder()
                .text("Comment")
                .build();
        CommentDto result = itemService.postComment(newUser2.getId(), savedItem.getId(), commentDto);

        assertNotNull(result);
        assertEquals("Comment", result.getText());
        assertEquals(newUser2.getName(), result.getAuthorName());
        assertTrue(commentRepository.findById(result.getId()).isPresent());
    }
}