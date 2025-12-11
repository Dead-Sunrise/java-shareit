package ru.practicum.shareit.booking.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.ValidationException;

import java.time.LocalDateTime;

@Component
public class BookingValidator {
    public void bookingValidate(BookingDto bookingDto) {
        if (bookingDto.getItemId() == null) {
            throw new ValidationException("Id предмета для бронирования должен быть указан");
        }
        if (bookingDto.getStart() == null || bookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата начала бронирования должна быть указана и быть в будущем");
        }
        if (bookingDto.getEnd() == null || bookingDto.getEnd().isBefore(bookingDto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть указана и быть позднее даты начала");
        }
    }

    public void approvedValidate(Boolean approved) {
        if (approved == null) {
            throw new ValidationException("Параметр approved должен быть указан");
        }
    }
}
