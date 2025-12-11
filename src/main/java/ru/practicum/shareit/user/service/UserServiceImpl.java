package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
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
        return userMapper.userToDto(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден.")));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new ConflictException("Этот email уже используется.");
        }
        User user = userMapper.dtoToUser(userDto);
        return userMapper.userToDto(userRepository.save(user));
    }

    @Override
    public UserDto updateUser(UserDto newUser, Long userId) {
        User oldUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден."));
        if (newUser.getEmail() != null &&
                !newUser.getEmail().equals(oldUser.getEmail()) &&
                userRepository.existsByEmail(newUser.getEmail())) {
            throw new ConflictException("Этот email уже используется.");
        }
        if (newUser.getEmail() != null) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        return userMapper.userToDto(userRepository.save(oldUser));
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }
}
