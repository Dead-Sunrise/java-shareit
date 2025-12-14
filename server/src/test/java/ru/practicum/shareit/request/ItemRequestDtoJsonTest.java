package ru.practicum.shareit.request;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemShortDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.dto.UserShortDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 12, 12, 10, 0, 0);
        UserShortDto user = UserShortDto.builder()
                .id(1L)
                .name("Requester")
                .build();
        ItemShortDto item1 = ItemShortDto.builder()
                .id(10L)
                .name("Item1")
                .build();
        ItemShortDto item2 = ItemShortDto.builder()
                .id(11L)
                .name("Item2")
                .build();
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Description")
                .user(user)
                .created(created)
                .items(List.of(item1, item2))
                .build();
        JsonContent<ItemRequestDto> result = json.write(dto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathNumberValue("$.user.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.user.name").isEqualTo("Requester");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-12-12T10:00:00");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(10);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Item1");
        assertThat(result).extractingJsonPathNumberValue("$.items[1].id").isEqualTo(11);
        assertThat(result).extractingJsonPathStringValue("$.items[1].name").isEqualTo("Item2");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\": 1, \"description\": \"Description\", \"user\": {\"id\": 1, \"name\": \"Requester\"}, "
                + "\"created\": \"2024-12-12T10:00:00\", \"items\": [{\"id\": 10, \"name\": \"Item1\"}, "
                + "{\"id\": 11, \"name\": \"Item2\"}]}";
        ItemRequestDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDescription()).isEqualTo("Description");
        assertThat(result.getUser()).isNotNull();
        assertThat(result.getUser().getId()).isEqualTo(1L);
        assertThat(result.getUser().getName()).isEqualTo("Requester");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 12, 10, 0, 0));
        assertThat(result.getItems()).hasSize(2);
        assertThat(result.getItems().get(0).getId()).isEqualTo(10L);
        assertThat(result.getItems().get(1).getId()).isEqualTo(11L);
    }

    @Test
    void testDeserializeWithEmptyItems() throws Exception {
        String content = "{\"id\": 1, \"description\": \"Description\", \"user\": {\"id\": 1, \"name\": \"Requester\"}, "
                + "\"created\": \"2024-12-12T10:00:00\", \"items\": []}";
        ItemRequestDto result = json.parseObject(content);
        assertThat(result.getItems()).isNotNull();
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void testDeserializeWithNullItems() throws Exception {
        String content = "{\"id\": 1, \"description\": \"Description\", \"user\": {\"id\": 1, \"name\": \"Requester\"}, "
                + "\"created\": \"2024-12-12T10:00:00\"}";
        ItemRequestDto result = json.parseObject(content);
        assertThat(result.getItems()).isNull();
    }
}