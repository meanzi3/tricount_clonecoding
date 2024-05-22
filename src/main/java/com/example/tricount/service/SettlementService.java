package com.example.tricount.service;

import com.example.tricount.MemberContext;
import com.example.tricount.model.*;
import com.example.tricount.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

  private final SettlementRepository settlementRepository;

  public Settlement createSettlement(String settlementName){
    // 정산방 이름을 받아서 정산방을 만듦
    Settlement settlement = settlementRepository.create(settlementName);
    // 정산방을 만든 사람은 참여자에 바로 추가
    settlementRepository.addParticipantToSettlement(settlement.getId(), MemberContext.getMember().getId());
    settlement.getParticipants().add(MemberContext.getMember());

    return settlement;
  }

  public void joinSettlement(Long settlementId){
    // 정산방이 있는 지 확인
    settlementRepository.findById(settlementId).orElseThrow(() -> new RuntimeException("settlement is not found"));

    // 현재 로그인 되어있는 회원을 읽어와서 참여자로 추가
    settlementRepository.addParticipantToSettlement(settlementId, MemberContext.getMember().getId());
  }

  // 정산 결과
  public List<BalanceResult> getBalanceResult(Long settlementId){
    // 특정 settlementId에 해당하는 지출 내역을 가져옴. 멤버별로 그룸화 함.
    Map<Member, List<ExpenseResult>> collect = settlementRepository.findExpensesWithMemberBySettlementId(settlementId)
            .stream().collect(Collectors.groupingBy(ExpenseResult::getPayerMember));

    // A: 1000, 2000
    // B: 1000

    // 각 멤버가 지출한 금액을 모두 더해서 memberAmountSumMap에 저장.
    Map<Member, BigDecimal> memberAmountSumMap = collect.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, memberListEntry ->
            memberListEntry.getValue().stream().map(ExpenseResult::getAmount).reduce(BigDecimal.ONE, BigDecimal::add)));

    // A: 3000
    // B: 1000

    // 전체 총 금액과 평균 금액 계산.
    BigDecimal sumAmount = memberAmountSumMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add); // 4000
    BigDecimal averageAmount = sumAmount.divide(BigDecimal.valueOf(memberAmountSumMap.size()), BigDecimal.ROUND_DOWN); // 2000

    // 각 멤버의 평균 금액 대비 차이 계산
    Map<Member, BigDecimal> calculatedAmountMap = memberAmountSumMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, memberBigDecimalEntry -> memberBigDecimalEntry.getValue().subtract(averageAmount)));

    // A: 3000 - 2000 : 1000
    // B: 1000 - 2000 : -1000

    // receiver와 sender 구분
    // receiver (평균 금액보다 더 지출한 멤버 / calculatedAmountMap의 값이 양수)
    List<Map.Entry<Member, BigDecimal>> receivers = calculatedAmountMap.entrySet().stream()
            .filter(memberBigDecimalEntry -> memberBigDecimalEntry.getValue().signum() > 0)
            .collect(Collectors.toList());

    // sender (평균 금액보다 덜 지출한 멤버 / calculatedAmountMap의 값이 음수)
    List<Map.Entry<Member, BigDecimal>> senders = calculatedAmountMap.entrySet().stream()
            .filter(memberBigDecimalEntry -> memberBigDecimalEntry.getValue().signum() < 0)
            .collect(Collectors.toList());

    // receiver ->  A: 1000, B: 1000, C: 1000
    // sender ->  D: -2000, E: -1000

    // 정산 계산 및 결과 생성
    List<BalanceResult> balanceResults = new ArrayList<>(); // 정산 결과를 저장할 리스트
    int receiverIndex = 0;
    int senderIndex = 0; // 현재 receiver와 sender를 가리키는 인덱스
    while(receiverIndex < receivers.size() && senderIndex < senders.size()){

      // 현재 receiver와 sender의 금액을 더해서 전송해야할 금액 계산
      // ex) A: 1000, B: -2000 이라면, amountToTransfer = -1000
      BigDecimal amountToTransfer = receivers.get(receiverIndex).getValue().add(senders.get(senderIndex).getValue());

      // receiver가 받을 금액이 sender가 지불할 금액 보다 크다면
      if(amountToTransfer.signum() < 0){
        balanceResults.add(new BalanceResult(
                senders.get(senderIndex).getKey().getId(),
                senders.get(senderIndex).getKey().getName(),
                receivers.get(receiverIndex).getValue().abs(),
                receivers.get(receiverIndex).getKey().getId(),
                receivers.get(receiverIndex).getKey().getName()
        )); // balanceResults 에 추가.
        receivers.get(receiverIndex).setValue(BigDecimal.ZERO); // receiver의 금색을 0으로
        senders.get(senderIndex).setValue(amountToTransfer); // sender의 금액을 amountToTransfer로 업데이트 (-1000)
        receiverIndex++;
      } // sender가 지불할 금액이 수취자가 받을 금액보다 크다면
        else if (amountToTransfer.signum() > 0){
        balanceResults.add(new BalanceResult(
                senders.get(senderIndex).getKey().getId(),
                senders.get(senderIndex).getKey().getName(),
                senders.get(senderIndex).getValue().abs(),
                receivers.get(receiverIndex).getKey().getId(),
                receivers.get(receiverIndex).getKey().getName()
        ));
        receivers.get(receiverIndex).setValue(amountToTransfer); // receiver의 금액을 amountToTransfer로 업데이트
        senders.get(senderIndex).setValue(BigDecimal.ZERO); // sender의 금액을 0으로 설정
        senderIndex++;
      } // 정확히 일치하는 경우
        else {
        balanceResults.add(new BalanceResult(
                senders.get(senderIndex).getKey().getId(),
                senders.get(senderIndex).getKey().getName(),
                senders.get(senderIndex).getValue().abs(),
                receivers.get(receiverIndex).getKey().getId(),
                receivers.get(receiverIndex).getKey().getName()
        ));
        receivers.get(receiverIndex).setValue(BigDecimal.ZERO);
        senders.get(senderIndex).setValue(BigDecimal.ZERO); // 둘다 0으로
        receiverIndex++;
        senderIndex++;
      }
    }

    // 정산 결과 반환
    return balanceResults;

  }
}
