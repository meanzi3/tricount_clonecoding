package com.example.tricount.repository;

import com.example.tricount.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

  // settlementId 별 참가 중인 모든 회원과 관련된 지출 내역을 조회
  // 각 회원의 지출 내역을 담은 ExpenseResult 객체를 리스트로 반환
  public List<ExpenseResult> findExpensesWithMemberBySettlementId(Long settlementId) {
    String sql = "SELECT * " +
            "FROM settlement_participant " +
            "JOIN member ON settlement_participant.member_id = member.id " +
            "LEFT JOIN expense ON settlement_participant.member_id = expense.payer_member_id " +
            "AND settlement_participant.settlement_id = expense.settlement_id " +
            "WHERE settlement_participant.settlement_id = ?";
    return jdbcTemplate.query(sql, expenseResultRowMapper(), settlementId);
  }

  // DB 조회 결과 ExpenseResult 객체로 매핑
  private RowMapper<ExpenseResult> expenseResultRowMapper() {
    return (rs, rowNum) -> {
      // ExpenseResult 객체 새로 생성
      ExpenseResult expenseResult = new ExpenseResult();
      // ResultSet에서 settlementId와 amount를 가져와 설정
      expenseResult.setSettlementId(rs.getLong("settlement_participant.settlement_id"));
      BigDecimal amt = rs.getBigDecimal("expense.amount");
      expenseResult.setAmount(amt != null ? amt : BigDecimal.ZERO);

      // Member 객체 새로 생성
      Member member = new Member();
      if(rs.getLong("member.id") != 0) {
        // ResultSet에서 member 테이블의 정보를 가져와 설정
        member.setId(rs.getLong("member.id"));
        member.setLoginId(rs.getString("member.login_id"));
        member.setPassword(rs.getString("member.password"));
        member.setName(rs.getString("member.name"));

        // payerMember 설정
        expenseResult.setPayerMember(member);
      }

      return expenseResult;
    };
  }

  // settlement 별 전체 지출 내역 조회
  public List<ExpenseResponse> findExpensesBySettlementId(Long settlementId) {
    String sql = "SELECT * " +
            "FROM settlement_participant " +
            "JOIN member ON settlement_participant.member_id = member.id " +
            "LEFT JOIN expense ON settlement_participant.member_id = expense.payer_member_id " +
            "AND settlement_participant.settlement_id = expense.settlement_id " +
            "WHERE settlement_participant.settlement_id = ?";
    return jdbcTemplate.query(sql, expenseResultRowMapper2(), settlementId);
  }

  private RowMapper<ExpenseResponse> expenseResultRowMapper2() {
    return (rs, rowNum) -> {

      // expense.amount를 가져와서 확인
      BigDecimal amt = rs.getBigDecimal("expense.amount");
      if (amt == null || amt.compareTo(BigDecimal.ZERO) == 0) {
        // amount가 0이거나 null인 경우 null을 반환하여 해당 행을 무시
        return null;
      }

      // ExpenseResponse 객체 새로 생성
      ExpenseResponse expenseResponse = new ExpenseResponse();
      // ResultSet 에서 name 과 amount, expenseDateTime 을 가져와 설정
      expenseResponse.setName(rs.getString("expense.name"));
      expenseResponse.setAmount(amt != null ? amt : BigDecimal.ZERO);
      expenseResponse.setExpenseDateTime(rs.getTimestamp(13).toLocalDateTime());

      // Member 객체 새로 생성
      Member member = new Member();
      if(rs.getLong("member.id") != 0) {
        // ResultSet에서 member 테이블의 정보를 가져와 설정
        member.setId(rs.getLong("member.id"));
        member.setLoginId(rs.getString("member.login_id"));
        member.setPassword(rs.getString("member.password"));
        member.setName(rs.getString("member.name"));

        // payerMember 설정
        expenseResponse.setPayerMember(member);
      }

      return expenseResponse;
    };
  }

}
