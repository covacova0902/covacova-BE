package com.covacova.member.domain;

public enum MemberStatus {
    PENDING,  //회원가입은 했지만 온보딩 등 필수 절차가 끝나지 않은 상태
    ACTIVE,   //정상 이용 가능한 회원
    WITHDRAWN  //탈퇴한 회원
}
