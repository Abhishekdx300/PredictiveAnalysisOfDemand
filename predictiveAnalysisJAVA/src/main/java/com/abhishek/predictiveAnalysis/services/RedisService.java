package com.abhishek.predictiveAnalysis.services;

import com.abhishek.predictiveAnalysis.entities.PredictionData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Random;
import java.util.UUID;

@Service
public class RedisService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private Random random;

    public  String saveDataModel(PredictionData predictionData) {
        int randomNumber = 100 + random.nextInt(900);
        String Key = UUID.randomUUID() + "27201" + randomNumber;
        redisTemplate.opsForValue().set(Key, predictionData);
        return Key;
    }
    public PredictionData getDataModel(String key) {
        return (PredictionData) redisTemplate.opsForValue().get(key);
    }
}
