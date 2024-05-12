package com.example.tricount.model;

import lombok.Data;

@Data
public class MemberLoginRequest {
  private String loginId;
  private String password;
}
