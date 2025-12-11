package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.service.BookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookingServiceImpl.class, BookingMapper.class})
public class BookingServiceImplTest {
    @Autowired
    private BookingService bookingService;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;
@Autowired
    private ItemRepository itemRepository;

    private User user1, user2;
    private Item newItem1;
    private Item newItem2;
    private BookingDto bookingDto1, bookingDto2, bookingDto3;

    @BeforeEach
    void setUp() {
        user1 = userRepository.save(User.builder()
                .id(1L)
                .name("Name1")
                .email("user1@yandex.ru")
                .build());
        user2 = userRepository.save(User.builder()
                .id(2L)
                .name("Name2")
                .email("user2@yandex.ru")
                .build());
        newItem1 = itemRepository.save(Item.builder()
                .id(1L)
                .name("Item1")
                .description("Description1")
                .available(true)
                .owner(user1)
                .build());
        newItem2 = itemRepository.save(Item.builder()
                .id(2L)
                .name("Item2")
                .description("Description2")
                .available(true)
                .owner(user2)
                .build());
        Item newItem3 = itemRepository.save(Item.builder()
                .id(3L)
                .name("Item3")
                .description("Description3")
                .available(true)
                .owner(user1)
                .build());
        bookingDto1 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(newItem1.getId())
                .build();
        bookingDto2 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(newItem2.getId())
                .build();
        bookingDto3 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(newItem3.getId())
                .build();
    }

    @Test
    void createBookingTest() {
        BookingSaveDto createBooking = bookingService.create(user2.getId(), bookingDto1);
        assertNotNull(createBooking);
        assertEquals(createBooking.getItem().getId(), newItem1.getId());
        assertEquals(createBooking.getBooker().getId(), user2.getId());
        assertEquals(BookingStatus.WAITING, createBooking.getStatus());
    }

    @Test
    void processingBookingTest() {
        BookingSaveDto createBooking1 = bookingService.create(user2.getId(), bookingDto1);
        BookingSaveDto createBooking2 = bookingService.create(user1.getId(), bookingDto2);
        BookingSaveDto resultBooking1 = bookingService.processingBookingResponse(user1.getId(), createBooking1.getId(), true);
        BookingSaveDto resultBooking2 = bookingService.processingBookingResponse(user2.getId(), createBooking2.getId(), false);
        assertEquals(resultBooking1.getItem().getId(), newItem1.getId());
        assertEquals(BookingStatus.APPROVED, resultBooking1.getStatus());
        assertEquals(resultBooking2.getItem().getId(), newItem2.getId());
        assertEquals(BookingStatus.REJECTED, resultBooking2.getStatus());
    }

    @Test
    void getBookingByIdTest() {
        BookingSaveDto createBooking = bookingService.create(user2.getId(), bookingDto1);
        BookingSaveDto resultBooking = bookingService.findBookingById(user2.getId(), createBooking.getId());
        assertNotNull(resultBooking);
        assertEquals(createBooking.getId(), resultBooking.getId());
    }

    @Test
    void getAllBookingsByUserTest() {
        BookingSaveDto createBooking1 = bookingService.create(user2.getId(), bookingDto1);
        BookingSaveDto createBooking2 = bookingService.create(user1.getId(), bookingDto2);
        BookingSaveDto createBooking3 = bookingService.create(user2.getId(), bookingDto3);
        List<BookingSaveDto> bookings = bookingService.findAllBookingsByUser(user2.getId(), BookingState.WAITING);
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
        assertTrue(bookings.contains(createBooking1));
        assertTrue(bookings.contains(createBooking3));
    }

    @Test
    void getAllBookingsByUserItemsTest() {
        BookingSaveDto createBooking1 = bookingService.create(user2.getId(), bookingDto1);
        BookingSaveDto createBooking2 = bookingService.create(user1.getId(), bookingDto2);
        BookingSaveDto createBooking3 = bookingService.create(user2.getId(), bookingDto3);
        List<BookingSaveDto> bookings = bookingService.findAllBookingsByUserItems(user1.getId(), BookingState.WAITING);
        assertNotNull(bookings);
        assertEquals(2, bookings.size());
        assertTrue(bookings.contains(createBooking1));
        assertTrue(bookings.contains(createBooking3));
    }
}