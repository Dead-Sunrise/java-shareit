package ru.practicum.shareit.user.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

@Component
public class UserValidator {

    public void validateEmail(UserDto userDto) {
        if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
            throw new ValidationException("Адрес почты не указан, либо пуст.");
        }
        if (!userDto.getEmail().contains("@")) {
            throw new ValidationException("Адрес почты указан в неверном формате.");
        }
    }

    public void validateName(UserDto userDto) {
        if (userDto.getName() == null || userDto.getName().isBlank()) {
            throw new ValidationException("Имя пользователя не указано либо пустое.");
        }
    }

    public void validateUser(UserDto userDto) {
        validateEmail(userDto);
        validateName(userDto);
    }

    public void validateUpdateUser(UserDto userDto) {
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!userDto.getEmail().contains("@")) {
                throw new ValidationException("Адрес почты указан в неверном формате.");
            }
        }
    }
}