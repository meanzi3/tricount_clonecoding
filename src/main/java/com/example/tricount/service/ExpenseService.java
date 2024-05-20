package com.example.tricount.service;

import com.example.tricount.model.Expense;
import com.example.tricount.model.ExpenseRequest;
import com.example.tricount.model.Settlement;
import com.example.tricount.repository.ExpenseRepository;
import com.example.tricount.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExpenseService {

  private final SettlementRepository settlementRepository;

  private final ExpenseRepository expenseRepository;

  public Expense addExpense(ExpenseRequest request){
    // 정산방이 있는 지 확인
    Optional<Settlement> settlementOptional = settlementRepository.findById(request.getSettlementId());
    if(!settlementOptional.isPresent()){
      throw new RuntimeException("settlement is not found");
    }

    // 해당 정산방에 참여한 회원인지 확인
    boolean isParticipant = settlementOptional.get().getParticipants()
            .stream().anyMatch(member -> member.getId().equals(request.getPayerMemberId()));
    if(!isParticipant){
      throw new RuntimeException("This member is not a participant in this settlement");
    }

    Expense expense = Expense.builder()
            .name(request.getName())
            .settlementId(request.getSettlementId())
            .payerMemberId(request.getPayerMemberId())
            .amount(request.getAmount())
            .expenseDateTime(LocalDateTime.now())
            .build();

    return expenseRepository.save(expense);
  }
}
