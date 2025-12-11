package ru.practicum.shareit.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.client.UserClient;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.validator.UserValidator;

@Controller
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {

    private final UserClient userClient;
    private final UserValidator userValidator;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        log.info("/users GET Запрос на получение списка пользователей.");
        return userClient.getAllUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getUserById(@PathVariable Long id) {
        log.info("/users/{id} GET Запрос на получение данных конкретного пользователя по id");
        return userClient.getUserById(id);
    }

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody UserDto userDto) {
        log.info("/users POST Запрос на создание нового пользователя");
        userValidator.validateUser(userDto);
        return userClient.createUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object> update(@PathVariable Long userId, @RequestBody UserDto newUser) {
        log.info("/users PUT Запрос на обновление пользователя.");
        userValidator.validateUpdateUser(newUser);
        return userClient.updateUser(newUser, userId);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        return userClient.deleteUserById(userId);
    }
}