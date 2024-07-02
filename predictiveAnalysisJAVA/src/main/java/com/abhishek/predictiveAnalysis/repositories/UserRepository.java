package com.abhishek.predictiveAnalysis.repositories;

import com.abhishek.predictiveAnalysis.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String>{
    public User findByEmail(String email);
}
