package com.example.tricount.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class Settlement {

  private Long id; // 정산 id
  private String name; // 정산 이름
  private List<Member> participants = new ArrayList<>(); // 정산에 참여한 유저들
}
