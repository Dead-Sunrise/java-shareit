package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Collection<UserDto> getAllUsers() {
        return userRepository.findAll().stream().map(userMapper::userToDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getUserById(Long id) {
        return userMapper.userToDto(userRepository.getUserById(id));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        User user = userMapper.dtoToUser(userDto);
        return userMapper.userToDto(userRepository.create(user));
    }

    @Override
    public UserDto updateUser(UserDto newUser, Long userId) {
        User user = userMapper.dtoToUser(newUser);
        return userMapper.userToDto(userRepository.update(user, userId));
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteUserById(id);
    }
}
