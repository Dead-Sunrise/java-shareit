package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
    private UserDto newUser1, newUser2, newUser3;

    @BeforeEach
    void setUp() {
        newUser1 = UserDto.builder()
                .name("Name1")
                .email("user1@yandex.ru")
                .build();
        newUser2 = UserDto.builder()
                .name("Name2")
                .email("user2@yandex.ru")
                .build();
        newUser3 = UserDto.builder()
                .name("Name3")
                .email("user3@yandex.ru")
                .build();
    }

    @Test
    void createNewUserTest() {
        UserDto user = userService.createUser(newUser1);
        assertEquals("Name1", user.getName());
        assertEquals("user1@yandex.ru", newUser1.getEmail());
        assertNotNull(user.getId());
    }

    @Test
    void createAndGetUserByIdTest() {
        UserDto savedUser = userService.createUser(newUser1);
        UserDto resultUser = userService.getUserById(savedUser.getId());
        assertNotNull(resultUser);
        assertEquals(savedUser.getId(), resultUser.getId());
        assertEquals(savedUser.getEmail(), resultUser.getEmail());
    }

    @Test
    void getAllUsersTest() {
        UserDto user1 = userService.createUser(newUser1);
        UserDto user2 = userService.createUser(newUser2);
        UserDto user3 = userService.createUser(newUser3);
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
    void updateUserTest() {
        UserDto user = userService.createUser(newUser1);
        UserDto updatedUser = userService.updateUser(newUser2, user.getId());
        assertEquals("Name2", updatedUser.getName());
        assertEquals("user2@yandex.ru", updatedUser.getEmail());
        assertEquals(user.getId(), updatedUser.getId());
    }

    @Test
    void deleteUserTest() {
        UserDto user = userService.createUser(newUser1);
        userService.deleteUserById(user.getId());
        assertThrows(NotFoundException.class, () -> userService.getUserById(user.getId()));
    }
}