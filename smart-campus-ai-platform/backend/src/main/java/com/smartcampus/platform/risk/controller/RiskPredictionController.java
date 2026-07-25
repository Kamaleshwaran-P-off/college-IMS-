package com.smartcampus.platform.risk.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.risk.dto.RiskPredictionRequest;
import com.smartcampus.platform.risk.dto.RiskPredictionResponse;
import com.smartcampus.platform.risk.service.RiskPredictionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/risk")
@Validated
public class RiskPredictionController {
  private final RiskPredictionService riskPredictionService;

  public RiskPredictionController(RiskPredictionService riskPredictionService) {
    this.riskPredictionService = riskPredictionService;
  }

  @PostMapping("/predict")
  public ResponseEntity<RiskPredictionResponse> predict(@Valid @RequestBody RiskPredictionRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(riskPredictionService.predict(request));
  }
}
