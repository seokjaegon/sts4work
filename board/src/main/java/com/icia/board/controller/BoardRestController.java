package com.icia.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.icia.board.dto.MemberDto;
import com.icia.board.service.MailService;
import com.icia.board.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BoardRestController {
	@Autowired
	private MemberService mServ;
	@Autowired
	private MailService mailServ;
	
	@GetMapping("idCheck")
	public String idCheck(@RequestParam("mid") String mid) {
		log.info("idCheck()");
		String res = mServ.idCheck(mid);
		return res;
	}
	
	@PostMapping("mailConfirm")
	public String mailConfirm(MemberDto member, HttpSession session) {
		log.info("mailConfirm()");
		String res = mailServ.sendEmail(member, session);
		
		return res;
	}
	
	@PostMapping("codeAuth")
	public String codeAuth(@RequestParam("v_code") String v_code,
						   HttpSession session) {
		log.info("codeAuth()");
		String res = mailServ.codeAuth(v_code, session);
		return res;
	}
}//class end
