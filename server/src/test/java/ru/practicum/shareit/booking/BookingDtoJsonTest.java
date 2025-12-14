package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> bookingDtoJson;

    @Autowired
    private JacksonTester<BookingSaveDto> bookingSaveDtoJson;

    @Test
    void testBookingDtoSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 12, 12, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 13, 10, 0, 0);
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .itemId(10L)
                .bookerId(20L)
                .status(BookingStatus.WAITING)
                .build();
        JsonContent<BookingDto> result = bookingDtoJson.write(bookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-12-12T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-12-13T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.itemId").isEqualTo(10);
        assertThat(result).extractingJsonPathNumberValue("$.bookerId").isEqualTo(20);
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
    }

    @Test
    void testBookingDtoDeserialize() throws Exception {
        String content = "{\"id\": 1, \"start\": \"2024-12-12T10:00:00\", \"end\": \"2024-12-13T10:00:00\", "
                + "\"itemId\": 10, \"bookerId\": 20, \"status\": \"WAITING\"}";
        BookingDto result = bookingDtoJson.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStart()).isEqualTo(LocalDateTime.of(2024, 12, 12, 10, 0, 0));
        assertThat(result.getEnd()).isEqualTo(LocalDateTime.of(2024, 12, 13, 10, 0, 0));
        assertThat(result.getItemId()).isEqualTo(10L);
        assertThat(result.getBookerId()).isEqualTo(20L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void testBookingDtoDeserializeWithCustomDateFormat() throws Exception {
        String content = "{\"id\": 1, \"start\": \"2024-12-12T10:00:00.000\", \"end\": \"2024-12-13T10:00:00.000Z\", "
                + "\"itemId\": 10, \"bookerId\": 20, \"status\": \"WAITING\"}";
        BookingDto result = bookingDtoJson.parseObject(content);
        assertThat(result.getStart()).isNotNull();
        assertThat(result.getEnd()).isNotNull();
    }

    @Test
    void testBookingSaveDtoSerialize() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 12, 12, 10, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 12, 13, 10, 0, 0);
        ItemShortDto item = ItemShortDto.builder()
                .id(10L)
                .name("Item Name")
                .build();
        UserShortDto booker = UserShortDto.builder()
                .id(20L)
                .name("Booker Name")
                .build();
        BookingSaveDto bookingSaveDto = BookingSaveDto.builder()
                .id(1L)
                .start(start)
                .end(end)
                .item(item)
                .booker(booker)
                .status(BookingStatus.APPROVED)
                .build();
        JsonContent<BookingSaveDto> result = bookingSaveDtoJson.write(bookingSaveDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2024-12-12T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2024-12-13T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Item Name");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(20);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Booker Name");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("APPROVED");
    }

    @Test
    void testBookingDtoWithNullDates() throws Exception {
        BookingDto bookingDto = BookingDto.builder()
                .id(1L)
                .start(null)
                .end(null)
                .itemId(10L)
                .bookerId(20L)
                .status(BookingStatus.WAITING)
                .build();
        JsonContent<BookingDto> result = bookingDtoJson.write(bookingDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isNull();
        assertThat(result).extractingJsonPathStringValue("$.end").isNull();
    }
}