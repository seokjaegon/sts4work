package com.icia.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.dto.MemberDto;
import com.icia.board.service.MemberService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class MemberController {
	@Autowired
	private MemberService mServ;
	
	@GetMapping("/")
	public String home() {
		log.info("home()");
		
		return "index";
	}
	
	@GetMapping("loginForm")
	public String loginForm() {
		log.info("loginForm()");
		
		return "loginForm";
	}
	
	@PostMapping("loginProc")
	public String loginProc(MemberDto member,
							HttpSession session,
							RedirectAttributes rttr) {
		log.info("loginProc()");
		
		return mServ.loginProc(member, session, rttr); 
	}
	
	@GetMapping("joinForm")
	public String joinForm() {
		log.info("joinForm()");
		
		return "joinForm";
	}
	
//	@GetMapping("idCheck")
//	@ResponseBody
//	public String idCheck(@RequestParam("mid") String mid) {
//		log.info("idCheck() {}", mid);
//		
//		return "ok";//javascript ajax success의 res로 들어가는 값.
//	}
}
