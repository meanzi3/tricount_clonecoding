package com.example.tricount.model;

import com.example.tricount.MemberContext;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseRequest {

  private String name; // 지출 이름
  private Long settlementId; // 정산방 이름
  private Long payerMemberId = MemberContext.getMember().getId(); // 지출한 멤버 아이디 (현재 로그인 된 멤버)
  private BigDecimal amount; // 지출액
}
