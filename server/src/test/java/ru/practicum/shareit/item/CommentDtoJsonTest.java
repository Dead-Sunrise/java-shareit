package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemShortDto;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class CommentDtoJsonTest {

    @Autowired
    private JacksonTester<CommentDto> json;

    @Test
    void testSerialize() throws Exception {
        LocalDateTime created = LocalDateTime.of(2024, 12, 12, 10, 30, 0);
        ItemShortDto item = ItemShortDto.builder()
                .id(1L)
                .name("Item")
                .build();
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("Comment")
                .authorName("Name")
                .item(item)
                .created(created)
                .build();
        JsonContent<CommentDto> result = json.write(commentDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.text").isEqualTo("Comment");
        assertThat(result).extractingJsonPathStringValue("$.authorName").isEqualTo("Name");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Item");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2024-12-12T10:30:00");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = "{\"id\": 1, \"text\": \"Comment\", \"authorName\": \"Name\", "
                + "\"item\": {\"id\": 1, \"name\": \"Item\"}, \"created\": \"2024-12-12T10:30:00\"}";
        CommentDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Comment");
        assertThat(result.getAuthorName()).isEqualTo("Name");
        assertThat(result.getItem()).isNotNull();
        assertThat(result.getItem().getId()).isEqualTo(1L);
        assertThat(result.getItem().getName()).isEqualTo("Item");
        assertThat(result.getCreated()).isEqualTo(LocalDateTime.of(2024, 12, 12, 10, 30, 0));
    }

    @Test
    void testDeserializeWithoutCreatedDate() throws Exception {
        String content = "{\"id\": 1, \"text\": \"Comment\", \"authorName\": \"Name\"}";
        CommentDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getText()).isEqualTo("Comment");
        assertThat(result.getAuthorName()).isEqualTo("Name");
        assertThat(result.getCreated()).isNull();
    }
}