package com.example.tricount.controller;

import com.example.tricount.model.Settlement;
import com.example.tricount.service.SettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SettlementController {

  private final SettlementService settlementService;

  @PostMapping("/settles/create")
  public ResponseEntity<Object> createSettlement(@RequestParam String settlementName){
    Settlement settlement = settlementService.createSettlement(settlementName);
    return new ResponseEntity<>(settlement, HttpStatus.OK);
  }

  @PostMapping("/settles/{id}/join")
  public ResponseEntity<Void> joinSettlement(@PathVariable Long id){
    settlementService.joinSettlement(id);

    return new ResponseEntity<>(HttpStatus.OK);
  }

  @GetMapping("/settles/{settlementId}")
  public ResponseEntity<Object> getSettlementExpenseResult(@PathVariable Long settlementId){
    return new ResponseEntity<>(settlementService.getExpenseResult(settlementId), HttpStatus.OK);
  }

  @GetMapping("/settles/{settlementId}/balance")
  public ResponseEntity<Object> getSettlementBalanceResult(@PathVariable Long settlementId){
    return new ResponseEntity<>(settlementService.getBalanceResult(settlementId), HttpStatus.OK);
  }

}


