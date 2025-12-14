package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.service.UserService;
import ru.practicum.shareit.user.service.UserServiceImpl;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({UserServiceImpl.class, UserMapper.class})
public class UserServiceImplTest {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    private UserDto userDto1, userDto2, userDto3;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        userDto1 = UserDto.builder()
                .name("Name1")
                .email("user1@yandex.ru")
                .build();
        userDto2 = UserDto.builder()
                .name("Name2")
                .email("user2@yandex.ru")
                .build();
        userDto3 = UserDto.builder()
                .name("Name3")
                .email("user3@yandex.ru")
                .build();
    }

    @Test
    void createNewUserTest() {
        UserDto user = userService.createUser(userDto1);
        assertEquals("Name1", user.getName());
        assertEquals("user1@yandex.ru", userDto1.getEmail());
        assertNotNull(user.getId());
    }

    @Test
    void createUserWithExistingEmail() {
        userService.createUser(userDto1);
        UserDto duplicateEmailUser = UserDto.builder()
                .name("Name")
                .email("user1@yandex.ru")
                .build();
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(duplicateEmailUser));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void createUserWithEmptyName() {
        UserDto userWithEmptyName = UserDto.builder()
                .name("")
                .email("user@yandex.ru")
                .build();
        UserDto user = userService.createUser(userWithEmptyName);
        assertNotNull(user);
        assertEquals("", user.getName());
    }

    @Test
    void getUserByIdTest() {
        UserDto savedUser = userService.createUser(userDto1);
        UserDto resultUser = userService.getUserById(savedUser.getId());
        assertNotNull(resultUser);
        assertEquals(savedUser.getId(), resultUser.getId());
        assertEquals(savedUser.getEmail(), resultUser.getEmail());
    }

    @Test
    void getUserByInvalidIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.getUserById(999L));
        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }

    @Test
    void getAllUsersTest() {
        userService.createUser(userDto1);
        userService.createUser(userDto2);
        userService.createUser(userDto3);
        Collection<UserDto> users = userService.getAllUsers();
        List<String> names = users.stream()
                .map(UserDto::getName)
                .toList();
        assertEquals(3, users.size());
        users.forEach(user -> {
            assertNotNull(user.getId());
            assertNotNull(user.getName());
            assertNotNull(user.getEmail());
        });
        assertTrue(names.contains("Name1"));
        assertTrue(names.contains("Name2"));
        assertTrue(names.contains("Name3"));
    }

    @Test
    void getAllUsersWhenEmptyTest() {
        Collection<UserDto> users = userService.getAllUsers();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    void updateUserTest() {
        UserDto user = userService.createUser(userDto1);
        UserDto updatedUser = userService.updateUser(userDto2, user.getId());
        assertEquals("Name2", updatedUser.getName());
        assertEquals("user2@yandex.ru", updatedUser.getEmail());
        assertEquals(user.getId(), updatedUser.getId());
    }

    @Test
    void updateUserByInvalidIdTest() {
        NotFoundException exception = assertThrows(NotFoundException.class,
                () -> userService.updateUser(userDto2, 999L));
        assertEquals("Пользователь с id 999 не найден.", exception.getMessage());
    }

    @Test
    void updateUserWithExistingEmailTest() {
        UserDto user1 = userService.createUser(userDto1);
        userService.createUser(userDto2);
        UserDto updateDto = UserDto.builder()
                .name("Updated Name")
                .email("user2@yandex.ru")
                .build();
        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.updateUser(updateDto, user1.getId()));
        assertEquals("Этот email уже используется.", exception.getMessage());
    }

    @Test
    void deleteUserTest() {
        UserDto user = userService.createUser(userDto1);
        userService.deleteUserById(user.getId());
        assertThrows(NotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}