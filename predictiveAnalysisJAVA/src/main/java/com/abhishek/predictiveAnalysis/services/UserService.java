package com.abhishek.predictiveAnalysis.services;

import com.abhishek.predictiveAnalysis.entities.User;
import com.abhishek.predictiveAnalysis.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    public List<User> getUsers() {
        return this.userRepository.findAll();
    }

    public User createUser(User user) {
        if (user == null || user.getPassword() == null || user.getPassword().isEmpty()) {
           return null;
        }

        User alreadyExists = userRepository.findByEmail(user.getEmail());
        if(Objects.nonNull(alreadyExists)){
            return null;
        }

        user.setUserId(UUID.randomUUID().toString());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }
}
