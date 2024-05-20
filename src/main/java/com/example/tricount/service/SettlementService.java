package com.example.tricount.service;

import com.example.tricount.MemberContext;
import com.example.tricount.model.Settlement;
import com.example.tricount.repository.SettlementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
