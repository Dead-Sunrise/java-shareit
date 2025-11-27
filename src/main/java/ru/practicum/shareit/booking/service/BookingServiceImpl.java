package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingSaveDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final BookingMapper bookingMapper;

    @Override
    public BookingSaveDto create(Long userId, BookingDto bookingDto) {
        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Предмет для бронирования не найден."));
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        if (!item.getAvailable()) {
            throw new ValidationException("Предмет недоступен для бронирования");
        }
        if (userId.equals(item.getOwner().getId())) {
            throw new ValidationException("Нельзя забронировать собственный предмет");
        }
        if (bookingDto.getStart() == null || bookingDto.getEnd() == null) {
            throw new ValidationException("Дата начала и окончания бронирования должны быть указаны.");
        }
        if (bookingDto.getStart().isBefore(LocalDateTime.now()) || bookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала и окончания бронирования должны быть в будущем");
        }
        Booking booking = bookingMapper.dtoToBooking(bookingDto, item, booker);
        booking.setStatus(BookingStatus.WAITING);
        return bookingMapper.bookingToBookingSaveDto(bookingRepository.save(booking));
    }

    @Override
    public BookingSaveDto processingBookingResponse(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с  id " + bookingId + " не найдено."));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException("Изменить статус бронирования может только владелец предмета.");
        }
        if (!booking.getStatus().equals(BookingStatus.WAITING)) {
            throw new ValidationException("Неверный статус бронирования для обработки.");
        }
        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        return bookingMapper.bookingToBookingSaveDto(bookingRepository.save(booking));
    }

    @Override
    public BookingSaveDto findBookingById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Бронирование с  id " + bookingId + " не найдено."));
        if (!booking.getItem().getOwner().getId().equals(userId) && !booking.getBooker().getId().equals(userId)) {
            throw new ValidationException("Информацию о бронировании может просматривать только владелец вещи либо автор бронирования");
        }
        return bookingMapper.bookingToBookingSaveDto(booking);
    }

    @Override
    public List<BookingSaveDto> findAllBookingsByUser(Long userId, BookingState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        LocalDateTime localDateTime = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case CURRENT -> bookings = bookingRepository
                    .findAllByBookerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, localDateTime, localDateTime);
            case PAST -> bookings = bookingRepository
                    .findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, localDateTime);
            case FUTURE -> bookings = bookingRepository
                    .findAllByBookerIdAndStartAfterOrderByStartDesc(userId, localDateTime);
            case WAITING -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository
                    .findAllByBookerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookings = bookingRepository
                    .findAllByBookerIdOrderByStartDesc(userId);
        }
        return bookings.stream().map(bookingMapper::bookingToBookingSaveDto).toList();
    }

    @Override
    public List<BookingSaveDto> findAllBookingsByUserItems(Long userId, BookingState state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        LocalDateTime localDateTime = LocalDateTime.now();
        List<Booking> bookings;
        switch (state) {
            case CURRENT -> bookings = bookingRepository
                    .findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByStartDesc(userId, localDateTime, localDateTime);
            case PAST -> bookings = bookingRepository
                    .findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, localDateTime);
            case FUTURE -> bookings = bookingRepository
                    .findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, localDateTime);
            case WAITING -> bookings = bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.WAITING);
            case REJECTED -> bookings = bookingRepository
                    .findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, BookingStatus.REJECTED);
            default -> bookings = bookingRepository
                    .findAllByItemOwnerIdOrderByStartDesc(userId);
        }
        return bookings.stream().map(bookingMapper::bookingToBookingSaveDto).toList();
    }
}

