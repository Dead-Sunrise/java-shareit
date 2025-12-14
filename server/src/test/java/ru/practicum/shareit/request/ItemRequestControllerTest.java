package ru.practicum.shareit.request;

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
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ItemRequestControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private ItemRequestService itemRequestService;

    private ItemRequestDto createTestRequestDto() {
        return ItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .build();
    }

    @Test
    void createRequestTest() throws Exception {
        Long userId = 1L;
        CreateItemRequestDto request = CreateItemRequestDto.builder()
                .description("Description")
                .build();
        ItemRequestDto createdRequest = createTestRequestDto();
        when(itemRequestService.create(request, userId)).thenReturn(createdRequest);
        mockMvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Description"));
        verify(itemRequestService).create(any(CreateItemRequestDto.class), eq(userId));
    }

    @Test
    void getAllRequestsByUserId() throws Exception {
        Long userId = 1L;
        List<ItemRequestDto> requests = List.of(createTestRequestDto());
        when(itemRequestService.getAllRequestsByUser(userId)).thenReturn(requests);
        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Description"));
        verify(itemRequestService).getAllRequestsByUser(userId);
    }

    @Test
    void getAllRequestByUserIdWhereMissingUserId() throws Exception {
        mockMvc.perform(get("/requests"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getAllRequestsTest() throws Exception {
        List<ItemRequestDto> requests = List.of(createTestRequestDto());
        when(itemRequestService.getAllRequests()).thenReturn(requests);
        mockMvc.perform(get("/requests/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].description").value("Description"));
        verify(itemRequestService).getAllRequests();
    }

    @Test
    void getRequestByIdTest() throws Exception {
        Long requestId = 1L;
        ItemRequestDto request = createTestRequestDto();
        when(itemRequestService.getRequestById(requestId)).thenReturn(request);
        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Description"));
        verify(itemRequestService).getRequestById(requestId);
    }

    @Test
    void getItemRequestByInvalidId() throws Exception {
        Long requestId = 999L;
        when(itemRequestService.getRequestById(requestId)).thenThrow(new NotFoundException("Запрос не найден"));
        mockMvc.perform(get("/requests/{requestId}", requestId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Запрос не найден"));
        verify(itemRequestService).getRequestById(requestId);
    }
}