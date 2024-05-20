package com.example.tricount.repository;

import com.example.tricount.model.Member;
import com.example.tricount.model.Settlement;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
@RequiredArgsConstructor
public class SettlementRepository {
  private final JdbcTemplate jdbcTemplate;

  // 정산 생성 (이름으로)
  public Settlement create(String name) {
    SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
            .withTableName("settlement").usingGeneratedKeyColumns("id");

    Map<String, Object> params = new HashMap<>();
    params.put("name", name);

    Number key = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource(params));

    Settlement settlement = new Settlement();
    settlement.setId(key.longValue());
    settlement.setName(name);

    return settlement;
  }

  // 정산 참여자 추가
  public void addParticipantToSettlement(Long settlementId, Long memberId) {
    jdbcTemplate.update("INSERT INTO settlement_participant (settlement_id, member_id) VALUES (?, ?)",
            settlementId, memberId);
  }

  // 정산 id로 조회
  public Optional<Settlement> findById(Long id) {
    List<Settlement> result = jdbcTemplate.query("select * from settlement "
            + "join settlement_participant on settlement.id = settlement_participant.settlement_id "
            + "join member on settlement_participant.member_id = member.id "
            + "where settlement.id = ?", settlementParticipantsRowMapper(), id);
    return result.stream().findAny();
  }

  // DB 조회 결과 Settlement 객체로 매핑
  private RowMapper<Settlement> settlementParticipantsRowMapper() {
    return ((rs, rowNum) -> {
      // 새로운 Settlement 객체 생성
      Settlement settlement = new Settlement();
      // ResultSet에서 id와 name을 가져와 설정
      settlement.setId(rs.getLong("settlement.id"));
      settlement.setName(rs.getString("settlement.name"));

      // 참여자 목록 생성
      List<Member> participants = new ArrayList<>();
      do {
        // ResultSet을 순회하면서 참여자를 추가한다.
        Member participant = new Member(
                rs.getLong("member.id"),
                rs.getString("member.login_id"),
                rs.getString("member.name"),
                rs.getString("member.password")
        );
        participants.add(participant);
      } while(rs.next());

      // 참여자 리스트를 settlement 객체에 설정, 반환
      settlement.setParticipants(participants);
      return settlement;
    });
  }

}
