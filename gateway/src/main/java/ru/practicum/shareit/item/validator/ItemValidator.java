package ru.practicum.shareit.item.validator;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@Component
public class ItemValidator {

    public void itemValidate(ItemDto itemDto) {
        if (itemDto.getName() == null || itemDto.getName().isBlank()) {
            throw new ValidationException("Название предмета не указано либо пустое.");
        }
        if (itemDto.getAvailable() == null) {
            throw new ValidationException("Возможность бронирования должна быть указана.");
        }
        if (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) {
            throw new ValidationException("Описание предмета не указано либо пустое.");
        }
    }

    public void commentValidate(CommentDto commentDto) {
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не указан либо пустой.");
        }
    }
}
