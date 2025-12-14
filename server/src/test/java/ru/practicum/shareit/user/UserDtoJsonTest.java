package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.user.dto.UserDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
public class UserDtoJsonTest {

    @Autowired
    private JacksonTester<UserDto> json;

    @Test
    void testSerialize() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .name("Name")
                .email("user@yandex.ru")
                .build();
        JsonContent<UserDto> result = json.write(userDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo("Name");
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo("user@yandex.ru");
    }

    @Test
    void testDeserialize() throws Exception {
        String content = """
            {
                "id": 1,
                "name": "Name",
                "email": "user@yandex.ru"
            }
            """;
        UserDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isEqualTo("user@yandex.ru");
    }

    @Test
    void testDeserializeWithMissingName() throws Exception {
        String content = """
            {
                "id": 1,
                "email": "user@yandex.ru"
            }
            """;
        UserDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isNull();
        assertThat(result.getEmail()).isEqualTo("user@yandex.ru");
    }

    @Test
    void testDeserializeWithMissingEmail() throws Exception {
        String content = """
            {
                "id": 1,
                "name": "Name"
            }
            """;
        UserDto result = json.parseObject(content);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Name");
        assertThat(result.getEmail()).isNull();
    }

    @Test
    void testDeserializeWithInvalidEmail() throws Exception {
        String content = """
            {
                "id": 1,
                "name": "Name",
                "email": "invalid-email"
            }
            """;
        UserDto result = json.parseObject(content);
        assertThat(result.getEmail()).isEqualTo("invalid-email");
    }

    @Test
    void testSerializeWithNullValues() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(null)
                .name(null)
                .email(null)
                .build();
        JsonContent<UserDto> result = json.write(userDto);
        assertThat(result).extractingJsonPathNumberValue("$.id").isNull();
        assertThat(result).extractingJsonPathStringValue("$.name").isNull();
        assertThat(result).extractingJsonPathStringValue("$.email").isNull();
    }
}