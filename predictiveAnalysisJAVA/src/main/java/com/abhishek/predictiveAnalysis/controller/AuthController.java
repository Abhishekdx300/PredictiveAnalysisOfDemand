package com.abhishek.predictiveAnalysis.controller;

import com.abhishek.predictiveAnalysis.entities.User;
import com.abhishek.predictiveAnalysis.models.CreateUserResponse;
import com.abhishek.predictiveAnalysis.models.JwtRequest;
import com.abhishek.predictiveAnalysis.models.JwtResponse;
import com.abhishek.predictiveAnalysis.security.JwtHelper;
import com.abhishek.predictiveAnalysis.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserService userService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private AuthenticationManager manager;;
    @Autowired
    private JwtHelper helper;


    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody JwtRequest request) {

        this.doAuthenticate(request.getEmail(), request.getPassword());
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = this.helper.generateToken(userDetails);

        JwtResponse response = JwtResponse.builder()
                .jwtToken(token)
                .username(userDetails.getUsername()).build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    //todo: need modification
     @PostMapping("/create-user")
     public ResponseEntity createUser (@RequestBody User user){
        try{
             User newuser =  userService.createUser(user);
             if(Objects.isNull(newuser)){
                 return new ResponseEntity(HttpStatus.BAD_REQUEST);
             }
             return new ResponseEntity(CreateUserResponse.builder()
                     .userId(newuser.getUserId())
                     .email(newuser.getEmail()).build(),HttpStatus.CREATED);
        }catch (Exception ex){
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void doAuthenticate(String email, String password) {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, password);
        try {
            manager.authenticate(authentication);
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException(" Invalid Username or Password  !!");
        }
    }

}
