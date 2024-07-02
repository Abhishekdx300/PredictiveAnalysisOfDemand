package com.abhishek.predictiveAnalysis.models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class CreateUserResponse {
private String email;
private String userId;
}
