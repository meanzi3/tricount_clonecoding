package com.example.tricount.controller;

import com.example.tricount.model.Expense;
import com.example.tricount.model.ExpenseRequest;
import com.example.tricount.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ExpenseController {

  private final ExpenseService expenseService;

  @PostMapping("/expense/add")
  public ResponseEntity<Object> addExpenseToSettlement(
          @RequestBody ExpenseRequest request
          ){
    Expense expense = expenseService.addExpense(request);
    return new ResponseEntity<>(expense, HttpStatus.OK);
  }
}
