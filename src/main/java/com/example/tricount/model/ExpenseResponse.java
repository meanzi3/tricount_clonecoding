package com.example.tricount.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ExpenseResponse {
  private String name;
  private Member payerMember;
  private BigDecimal amount;
  private LocalDateTime expenseDateTime;
}
