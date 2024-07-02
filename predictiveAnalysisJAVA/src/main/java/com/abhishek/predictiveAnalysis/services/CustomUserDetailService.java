package com.abhishek.predictiveAnalysis.services;

import com.abhishek.predictiveAnalysis.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
     @Autowired
     private UserRepository userRepository;
    @Override
    //todo: might give issue findByEmail return type changed
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findByEmail(username);
    }
}
