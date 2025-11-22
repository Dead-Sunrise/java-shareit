package ru.practicum.shareit.user.repository;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        try {
            for (User user1 : users.values()) {
                if (user.getEmail() != null && user.getEmail().equals(user1.getEmail())) {
                    throw new ConflictException("Этот email уже используется.");
                }
            }
            user.setId(getNextId());
            users.put(user.getId(), user);
            return user;
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @Override
    public User update(User newUser, Long userId) {
        if (userId == null) {
            throw new ValidationException("Id должен быть указан.");
        }
        if (users.containsKey(userId)) {
            for (User user1 : users.values()) {
                if (newUser.getEmail() != null && newUser.getEmail().equals(user1.getEmail()) && !userId.equals(user1.getId())) {
                    throw new ConflictException("Этот email уже используется.");
                }
            }
            User oldUser = users.get(userId);
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + userId + " не найден");
    }

    @Override
    public User getUserById(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public void deleteUserById(Long id) {
        if (id == null || !users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        users.remove(id);
    }
}
