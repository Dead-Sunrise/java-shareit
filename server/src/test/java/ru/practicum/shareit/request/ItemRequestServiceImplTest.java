package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({ItemRequestServiceImpl.class, ItemRequestMapper.class, ItemMapper.class, UserMapper.class})
public class ItemRequestServiceImplTest {
    @Autowired
    private ItemRequestService itemRequestService;
    @Autowired
    private UserRepository userRepository;

    private User newUser1, newUser2;
    private CreateItemRequestDto createItemRequestDto1, createItemRequestDto2;

    @BeforeEach
    void setUp() {
        createItemRequestDto1 = CreateItemRequestDto.builder()
                .description("Description1")
                .build();
        createItemRequestDto2 = CreateItemRequestDto.builder()
                .description("Description2")
                .build();
        newUser1 = userRepository.save(User.builder()
                .id(1L)
                .name("Name1")
                .email("user1@yandex.ru")
                .build());
        newUser2 = userRepository.save(User.builder()
                .id(2L)
                .name("Name2")
                .email("user2@yandex.ru")
                .build());
    }

    @Test
    void createItemRequest() {
        ItemRequestDto itemRequest = itemRequestService.create(createItemRequestDto1, newUser1.getId());
        assertNotNull(itemRequest.getId());
        assertEquals(newUser1.getId(), itemRequest.getUser().getId());
        assertEquals("Description1", itemRequest.getDescription());
    }

    @Test
    void getAllUserRequests() {
        ItemRequestDto itemRequest1 = itemRequestService.create(createItemRequestDto1, newUser1.getId());
        ItemRequestDto itemRequest2 = itemRequestService.create(createItemRequestDto2, newUser1.getId());
        List<ItemRequestDto> itemRequests = itemRequestService.getAllRequestsByUser(newUser1.getId());
        assertEquals(2, itemRequests.size());
        List<String> descriptions = itemRequests.stream()
                .map(ItemRequestDto::getDescription)
                .toList();
        assertTrue(descriptions.contains("Description1"));
        assertTrue(descriptions.contains("Description2"));
    }

    @Test
    void getAllRequestsTest() {
        ItemRequestDto itemRequest1 = itemRequestService.create(createItemRequestDto1, newUser1.getId());
        ItemRequestDto itemRequest2 = itemRequestService.create(createItemRequestDto2, newUser1.getId());
        ItemRequestDto itemRequest3 = itemRequestService.create(createItemRequestDto1, newUser2.getId());
        List<ItemRequestDto> requests = itemRequestService.getAllRequests();
        assertEquals(3, requests.size());
        requests.forEach(request -> {
            assertNotNull(request.getId());
            assertNotNull(request.getDescription());
            assertNotNull(request.getCreated());
        });
    }

    @Test
    void getItemRequestById() {
        ItemRequestDto itemRequest = itemRequestService.create(createItemRequestDto1, newUser1.getId());
        ItemRequestDto resultRequest = itemRequestService.getRequestById(itemRequest.getId());
        assertNotNull(resultRequest);
        assertEquals(itemRequest.getId(), resultRequest.getId());
        assertEquals(itemRequest.getDescription(), resultRequest.getDescription());
    }
}