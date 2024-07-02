package com.abhishek.predictiveAnalysis.repositories;

import com.abhishek.predictiveAnalysis.entities.PredictionData;
import org.springframework.data.repository.CrudRepository;

public interface PredictionDataRepo extends CrudRepository<PredictionData,String> {
}
