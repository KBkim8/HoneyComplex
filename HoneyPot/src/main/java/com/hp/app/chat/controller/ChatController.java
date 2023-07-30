package com.hp.app.chat.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ChatController {

	// 채팅 친구 목록 화면
	@GetMapping("chat/followerList")
	public String followerList() {
		return "/chat/followerList";
	}
	
	// 채팅방 목록 조회 화면
	@GetMapping("chat/list")
	public String chatList() {
		return "/chat/list";
	}
	
	
}
