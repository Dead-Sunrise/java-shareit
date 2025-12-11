package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithCommentsAndBookings;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemService itemService;

    private ItemDto createTestItemDto(Long id, String name, String description, boolean available) {
        return ItemDto.builder()
                .id(id)
                .name(name)
                .description(description)
                .available(available)
                .build();
    }

    private CommentDto createTestCommentDto() {
        return CommentDto.builder()
                .id(1L)
                .text("Comment")
                .authorName("Author")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void createValidItemTest() throws Exception {
        Long userId = 1L;
        ItemDto itemDto = createTestItemDto(1L, "Name", "Description", true);
        ItemDto createdItem = createTestItemDto(1L, "Name", "Description", true);
        when(itemService.create(any(ItemDto.class), eq(userId)))
                .thenReturn(createdItem);
        mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name"));
        verify(itemService).create(any(ItemDto.class), eq(userId));
    }

    @Test
    void updateValidItemTest() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        ItemDto itemDto = createTestItemDto(itemId, "Name", "Description", true);
        ItemDto updatedItem = createTestItemDto(itemId, "Name", "Description", true);
        when(itemService.update(any(ItemDto.class), eq(userId), eq(itemId))).thenReturn(updatedItem);
        mockMvc.perform(patch("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.available").value(true));
        verify(itemService).update(any(ItemDto.class), eq(userId), eq(itemId));
    }

    @Test
    void validSearchItemTest() throws Exception {
        Long userId = 1L;
        String search = "Name1";
        List<ItemDto> searchingItems = List.of(createTestItemDto(1L, "Name1", "Description1", true));
        when(itemService.search(search)).thenReturn(searchingItems);
        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", search))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Description1"));
        verify(itemService).search(search);
    }

    @Test
    void invalidSearchItemTest() throws Exception {
        Long userId = 1L;
        String search = "invalid text";
        when(itemService.search(search))
                .thenReturn(List.of());

        mockMvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", userId)
                        .param("text", search))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(itemService).search(search);
    }

    @Test
    void getAllUserItemsTest() throws Exception {
        Long userId = 1L;
        List<ItemDtoWithCommentsAndBookings> userItems = List.of(ItemDtoWithCommentsAndBookings.builder()
                .id(1L)
                .name("Name1")
                .description("Description1")
                .available(true)
                .ownerId(userId).build());
        when(itemService.getAllUserItems(userId)).thenReturn(userItems);
        mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Name1"))
                .andExpect(jsonPath("$[0].description").value("Description1"));
        verify(itemService).getAllUserItems(userId);
    }

    @Test
    void getAllUserItemsWhenMissingUserIdTest() throws Exception {
        mockMvc.perform(get("/items"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getItemByIdTest() throws Exception {
        Long itemId = 1L;
        Long userId = 1L;
        ItemDtoWithCommentsAndBookings item = ItemDtoWithCommentsAndBookings.builder()
                .id(itemId)
                .name("Name1")
                .description("Description1")
                .available(true)
                .ownerId(userId).build();
        when(itemService.getItemById(itemId, userId)).thenReturn(item);
        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Name1"))
                .andExpect(jsonPath("$.description").value("Description1"));
        verify(itemService).getItemById(itemId, userId);
    }

    @Test
    void getItemByInvalidIdTest() throws Exception {
        Long userId = 1L;
        Long itemId = 999L;
        when(itemService.getItemById(itemId, userId)).thenThrow(new NotFoundException("Предмет не найден"));
        mockMvc.perform(get("/items/{itemId}", itemId)
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Предмет не найден"));
        verify(itemService, times(1)).getItemById(itemId, userId);
    }

    @Test
    void postCommentTest() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        CommentDto commentDto = CommentDto.builder()
                .text("Comment")
                .build();
        CommentDto createdComment = createTestCommentDto();
        when(itemService.postComment(userId, itemId, commentDto))
                .thenReturn(createdComment);
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Comment"))
                .andExpect(jsonPath("$.authorName").value("Author"));
        verify(itemService).postComment(userId, itemId, commentDto);
    }

    @Test
    void deleteItemByIdTest() throws Exception {
        Long itemId = 1L;
        doNothing().when(itemService).deleteItemById(itemId);
        mockMvc.perform(delete("/items/{itemId}", itemId))
                .andExpect(status().isOk());
        verify(itemService).deleteItemById(itemId);
    }
}