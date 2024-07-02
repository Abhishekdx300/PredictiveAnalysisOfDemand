package com.abhishek.predictiveAnalysis.repositories;

import com.abhishek.predictiveAnalysis.entities.PredictiveAnalysisResult;
import org.springframework.data.repository.CrudRepository;

public interface PredictiveAnalysisResultRepo extends CrudRepository<PredictiveAnalysisResult,Long> {
    public PredictiveAnalysisResult findByRequestKey(String requestKey);
}
