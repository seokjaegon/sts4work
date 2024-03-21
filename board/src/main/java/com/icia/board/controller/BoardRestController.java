package com.icia.board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.icia.board.service.MemberService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class BoardRestController {
	@Autowired
	private MemberService mServ;
	
	@GetMapping("idCheck")
	public String idCheck(@RequestParam("mid") String mid) {
		log.info("idCheck()");
		String res = mServ.idCheck(mid);
		return res;
	}
}
