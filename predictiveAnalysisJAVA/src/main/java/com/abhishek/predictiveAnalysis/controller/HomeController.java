package com.abhishek.predictiveAnalysis.controller;


import com.abhishek.predictiveAnalysis.entities.PredictionData;
import com.abhishek.predictiveAnalysis.entities.PredictiveAnalysisResult;
import com.abhishek.predictiveAnalysis.models.GetResponseData;
import com.abhishek.predictiveAnalysis.services.RedisService;
import com.abhishek.predictiveAnalysis.services.ResultDataService;
import com.abhishek.predictiveAnalysis.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private UserService userService;
    @Autowired
    private ResultDataService resultDataService;
    @Autowired
    private RedisService redisService;
//    private PredictionDataService predictionDataService;

    @PostMapping("/data")
    public ResponseEntity saveNic(@RequestBody PredictionData data){
        try{
            // need to validate demand date

            String key = redisService.saveDataModel(data);
            System.out.println(data);
            return new ResponseEntity<>(key,HttpStatus.CREATED);
        } catch (Exception ex){
            ex.printStackTrace();
            return new ResponseEntity<>("Exception in Pushing Data",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/data/{requestKey}")
    public ResponseEntity getDataFromDB(@PathVariable String requestKey){
        try{

            PredictiveAnalysisResult fetchedData = resultDataService.getResultData(requestKey);

            if(Objects.isNull(fetchedData)){
                return new ResponseEntity<>("No saved data found", HttpStatus.BAD_REQUEST);
            }
            Map<String,Object> parsedData =fetchedData.getResultSet();

            return new ResponseEntity<>(GetResponseData.builder()
                    .requestKey(fetchedData.getRequestKey())
                    .resultSet(parsedData)
                    .noOfTimeAccessed(fetchedData.getNoOfTimeAccessed())
                    .lastAccessedOn(fetchedData.getLastAccessedOn()).build(),HttpStatus.CREATED);
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new ResponseEntity<>("Exception in Database fetching service",HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}
