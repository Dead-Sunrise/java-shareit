package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingSaveDto;

import java.util.List;

public interface BookingService {
    BookingSaveDto create(Long userId, BookingDto bookingDto);

    BookingSaveDto processingBookingResponse(Long ownerId, Long bookingId, boolean approved);

    BookingSaveDto findBookingById(Long userId, Long bookingId);

    List<BookingSaveDto> findAllBookingsByUser(Long userId, BookingState state);

    List<BookingSaveDto> findAllBookingsByUserItems(Long userId, BookingState state);
}
