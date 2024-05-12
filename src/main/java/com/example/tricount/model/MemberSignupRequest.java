package com.example.tricount.model;

import lombok.Data;

@Data
public class MemberSignupRequest {
  private String loginId;
  private String password;
  private String name;
}
