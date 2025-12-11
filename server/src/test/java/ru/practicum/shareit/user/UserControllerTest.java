package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private UserService userService;

    private UserDto createUserDto(Long id, String name, String email) {
        return UserDto.builder()
                .id(id)
                .name(name)
                .email(email)
                .build();
    }

    @Test
    void createValidUser() throws Exception {
        UserDto userDto = UserDto.builder()
                .name("Name")
                .email("user@yandex.ru")
                .build();

        UserDto createdUser = UserDto.builder()
                .id(1L)
                .name("Name")
                .email("user@yandex.ru")
                .build();

        when(userService.createUser(any(UserDto.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.email").value("user@yandex.ru"));
    }

    @Test
    void updateUser() throws Exception {
        Long userId = 1L;
        UserDto userDto = UserDto.builder()
                .name("Name")
                .email("user@yandex.ru")
                .build();

        UserDto updatedUser = UserDto.builder()
                .id(userId)
                .name("Updated Name")
                .email("updated@yandex.ru")
                .build();
        when(userService.updateUser(any(UserDto.class), eq(userId))).thenReturn(updatedUser);

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.email").value("updated@yandex.ru"));
    }

    @Test
    void updateUserWithDuplicateEmailTest() throws Exception {
        Long userId = 1L;
        UserDto updateRequest = UserDto.builder()
                .name("Updated Name")
                .email("existing@yandex.ru")
                .build();
        when(userService.updateUser(any(UserDto.class), eq(userId)))
                .thenThrow(new ConflictException("Email уже существует"));

        mockMvc.perform(patch("/users/{userId}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email уже существует"));
        verify(userService, times(1)).updateUser(any(UserDto.class), eq(userId));
    }

    @Test
    void getAllUsersTest() throws Exception {
        List<UserDto> users = List.of(createUserDto(1L, "Name1", "user1@yandex.ru"),
                createUserDto(2L, "Name2", "user2@yandex.ru"));
        when(userService.getAllUsers()).thenReturn(users);
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Name1"))
                .andExpect(jsonPath("$[0].email").value("user1@yandex.ru"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Name2"))
                .andExpect(jsonPath("$[1].email").value("user2@yandex.ru"));
        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsersWhenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(userService).getAllUsers();
    }

    @Test
    void getUserByIdTest() throws Exception {
        Long userId = 1L;
        UserDto userDto = createUserDto(1L, "Name1", "user1@yandex.ru");
        when(userService.getUserById(userId)).thenReturn(userDto);
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Name1"))
                .andExpect(jsonPath("$.email").value("user1@yandex.ru"));
        verify(userService).getUserById(userId);
    }

    @Test
    void getUserByInvalidIdTest() throws Exception {
        Long userId = 999L;
        when(userService.getUserById(userId)).thenThrow(new NotFoundException("Пользователь не найден"));
        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Пользователь не найден"));
        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void deleteUserByIdTest() throws Exception {
        Long userId = 1L;
        doNothing().when(userService).deleteUserById(userId);
        mockMvc.perform(delete("/users/{userId}", userId))
                .andExpect(status().isOk());
        verify(userService).deleteUserById(userId);
    }
}
