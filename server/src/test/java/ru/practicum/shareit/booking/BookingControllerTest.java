package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.service.BookingState;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class BookingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingService bookingService;

    private BookingDto createTestBookingDto() {
        return BookingDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .bookerId(1L)
                .build();
    }

    private BookingSaveDto createTestBookingSaveDto() {
        return BookingSaveDto.builder()
                .id(1L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .item(ItemShortDto.builder().id(1L).name("Name").build())
                .booker(UserShortDto.builder().id(1L).name("Name").build())
                .status(BookingStatus.WAITING)
                .build();
    }

    @Test
    void createBookingTest() throws Exception {
        Long userId = 1L;
        BookingDto booking = createTestBookingDto();
        BookingSaveDto bookingSaveDto = createTestBookingSaveDto();
        when(bookingService.create(eq(userId), any(BookingDto.class))).thenReturn(bookingSaveDto);
        mockMvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(booking)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
        verify(bookingService).create(eq(userId), any(BookingDto.class));
    }

    @Test
    void processingBookingTest() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        BookingSaveDto booking = createTestBookingSaveDto();
        booking.setStatus(BookingStatus.APPROVED);
        when(bookingService.processingBookingResponse(userId, booking.getId(), true)).thenReturn(booking);
        mockMvc.perform(patch("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
        verify(bookingService).processingBookingResponse(userId, bookingId, true);
    }

    @Test
    void getBookingByIdTest() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        BookingSaveDto bookingSaveDto = createTestBookingSaveDto();
        when(bookingService.findBookingById(userId, bookingId)).thenReturn(bookingSaveDto);
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("WAITING"));
        verify(bookingService).findBookingById(userId, bookingId);
    }

    @Test
    void getBookingByInvalidId() throws Exception {
        Long userId = 1L;
        Long bookingId = 999L;
        when(bookingService.findBookingById(userId, bookingId)).thenThrow(new NotFoundException("Бронирование не найдено"));
        mockMvc.perform(get("/bookings/{bookingId}", bookingId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Бронирование не найдено"));
        verify(bookingService).findBookingById(userId, bookingId);
    }

    @Test
    void findAllBookingsByUserTest() throws Exception {
        Long userId = 1L;
        List<BookingSaveDto> bookings = List.of(createTestBookingSaveDto());
        when(bookingService.findAllBookingsByUser(userId, BookingState.WAITING)).thenReturn(bookings);
        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
        verify(bookingService).findAllBookingsByUser(userId, BookingState.WAITING);
    }

    @Test
    void findAllBookingsByUserItemTest() throws Exception {
        Long userId = 1L;
        List<BookingSaveDto> bookings = List.of(createTestBookingSaveDto());
        when(bookingService.findAllBookingsByUserItems(userId, BookingState.WAITING)).thenReturn(bookings);
        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", userId)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L));
        verify(bookingService).findAllBookingsByUserItems(userId, BookingState.WAITING);
    }
}

