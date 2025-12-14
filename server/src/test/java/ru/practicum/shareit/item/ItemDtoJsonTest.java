package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class ItemDtoJsonTest {

    @Autowired
    private JacksonTester<ItemDto> json;

    @Test
    void testSerializeWithoutRequestId() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .ownerId(10L)
                .requestId(null)
                .build();
        JsonContent<ItemDto> result = json.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Description");
        assertThat(result).extractingJsonPathBooleanValue("$.available").isTrue();
        assertThat(result).extractingJsonPathNumberValue("$.ownerId").isEqualTo(10);
        assertThat(result).doesNotHaveJsonPath("$.requestId");
    }

    @Test
    void testSerializeWithRequestId() throws Exception {
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("Name")
                .description("Description")
                .available(true)
                .ownerId(10L)
                .requestId(100L)
                .build();
        JsonContent<ItemDto> result = json.write(itemDto);
        assertThat(result).extractingJsonPathNumberValue("$.requestId").isEqualTo(100);
    }

    @Test
    void testDeserializeWithRequestId() throws Exception {
        String content = """
            {
                "id": 1,
                "name": "Name",
                "description": "Description",
                "available": true,
                "ownerId": 10,
                "requestId": 100
            }
            """;
        ItemDto result = json.parseObject(content);
        assertThat(result.getRequestId()).isEqualTo(100L);
    }

    @Test
    void testDeserializeWithoutRequestId() throws Exception {
        String content = """
            {
                "id": 1,
                "name": "Name",
                "description": "Description",
                "available": true,
                "ownerId": 10
            }
            """;
        ItemDto result = json.parseObject(content);
        assertThat(result.getRequestId()).isNull();
    }
}