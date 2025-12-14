package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
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
    private Item newItem1, newItem2, newItem3;
    private BookingDto bookingDto1, bookingDto2, bookingDto3;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();

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
        newItem3 = itemRepository.save(Item.builder()
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
    void createValidBookingTest() {
        BookingSaveDto createBooking = bookingService.create(user2.getId(), bookingDto1);
        assertNotNull(createBooking);
        assertEquals(createBooking.getItem().getId(), newItem1.getId());
        assertEquals(createBooking.getBooker().getId(), user2.getId());
        assertEquals(BookingStatus.WAITING, createBooking.getStatus());
    }

    @Test
    void createBookingOnOwnItem() {
        Long userId = user1.getId();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(userId, bookingDto1));
        assertEquals("Нельзя забронировать собственный предмет", exception.getMessage());
    }

    @Test
    void createBookingOnUnavailableItem() {
        newItem1.setAvailable(false);
        itemRepository.save(newItem1);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(user2.getId(), bookingDto1));
        assertEquals("Предмет недоступен для бронирования", exception.getMessage());
    }

    @Test
    void createBookingWithPastDate() {
        BookingDto pastBookingDto = BookingDto.builder()
                .start(LocalDateTime.now().minusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(newItem1.getId())
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(user2.getId(), pastBookingDto));
        assertEquals("Дата начала и окончания бронирования должны быть в будущем", exception.getMessage());
    }

    @Test
    void createBookingWithNullDate() {
        BookingDto nullDatesBookingDto = BookingDto.builder()
                .start(null)
                .end(null)
                .itemId(newItem1.getId())
                .build();
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.create(user2.getId(), nullDatesBookingDto));
        assertEquals("Дата начала и окончания бронирования должны быть указаны.", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidUser() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(999L, bookingDto1));
        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }

    @Test
    void createBookingWithInvalidItem() {
        BookingDto nonExistentItemDto = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(999L)
                .build();
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.create(user2.getId(), nonExistentItemDto));
        assertEquals("Предмет для бронирования не найден.", exception.getMessage());
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
    void processingBookingByNonOwner() {
        BookingSaveDto createBooking1 = bookingService.create(user2.getId(), bookingDto1);
        Long wrongOwnerId = 999L;
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.processingBookingResponse(wrongOwnerId, createBooking1.getId(), true));
        assertEquals("Изменить статус бронирования может только владелец предмета.", exception.getMessage());
    }

    @Test
    void processingNonExistentBookingShouldThrowException() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.processingBookingResponse(user1.getId(), 999L, true));
        assertEquals("Бронирование с  id 999 не найдено.", exception.getMessage());
    }

    @Test
    void processingAlreadyProcessedBookingShouldThrowException() {
        BookingSaveDto createBooking1 = bookingService.create(user2.getId(), bookingDto1);
        bookingService.processingBookingResponse(user1.getId(), createBooking1.getId(), true);
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.processingBookingResponse(user1.getId(), createBooking1.getId(), true));
        assertEquals("Неверный статус бронирования для обработки.", exception.getMessage());
    }


    @Test
    void getBookingByIdTest() {
        BookingSaveDto createBooking = bookingService.create(user2.getId(), bookingDto1);
        BookingSaveDto resultBooking = bookingService.findBookingById(user2.getId(), createBooking.getId());
        assertNotNull(resultBooking);
        assertEquals(createBooking.getId(), resultBooking.getId());
    }

    @Test
    void getBookingByIdByUnauthorizedUserTest() {
        BookingSaveDto createBooking = bookingService.create(user2.getId(), bookingDto1);
        Long unauthorizedUserId = 999L;
        ValidationException exception = assertThrows(ValidationException.class,
                () -> bookingService.findBookingById(unauthorizedUserId, createBooking.getId()));
        assertEquals("Информацию о бронировании может просматривать только владелец вещи либо автор бронирования", exception.getMessage());
    }

    @Test
    void getBookingByInvalidIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.findBookingById(user2.getId(), 999L));
        assertEquals("Бронирование с  id 999 не найдено.", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllBookingsByUserWithAllStatesTest(BookingState state) {
        bookingService.create(user2.getId(), bookingDto1);
        bookingService.create(user2.getId(), bookingDto3);
        Item itemForUser2 = itemRepository.save(Item.builder()
                .name("Item4")
                .description("Description4")
                .available(true)
                .owner(user2)
                .build());
        BookingDto bookingDtoForUser1 = BookingDto.builder()
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(itemForUser2.getId())
                .build();
        bookingService.create(user1.getId(), bookingDtoForUser1);
        List<BookingSaveDto> bookings = bookingService.findAllBookingsByUser(user1.getId(), state);
        assertNotNull(bookings);
        if (state == BookingState.WAITING) {
            assertEquals(1, bookings.size());
        }
    }

    @Test
    void findAllBookingsByInvalidUserIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.findAllBookingsByUser(999L, BookingState.ALL));
        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(BookingState.class)
    void findAllBookingsByUserItemsWithAllStatesTest(BookingState state) {
        bookingService.create(user2.getId(), bookingDto1);
        bookingService.create(user2.getId(), bookingDto3);
        List<BookingSaveDto> bookings = bookingService.findAllBookingsByUserItems(user1.getId(), state);
        assertNotNull(bookings);
        if (state == BookingState.WAITING) {
            assertEquals(2, bookings.size());
        }
    }

    @Test
    void findAllBookingsByUserItemsWithInvalidUserIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> bookingService.findAllBookingsByUserItems(999L, BookingState.ALL));
        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }
}