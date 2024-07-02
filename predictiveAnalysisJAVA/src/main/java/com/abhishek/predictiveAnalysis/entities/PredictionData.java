package com.abhishek.predictiveAnalysis.entities;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PredictionData {
    private String category;
    private String subCategory;
    private int expectedPeriodInMonth;
    private int outDataPattern;
    private ArrayList<String> demandDate;
    private ArrayList<Integer> demandQty;
    private int analysisYear;
}
