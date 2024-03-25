package com.icia.board.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.dto.BoardDto;
import com.icia.board.dto.SearchDto;
import com.icia.board.service.BoardService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class BoardController {
	@Autowired
	private BoardService bServ;
	
	@GetMapping("boardList")
	public String boardList(SearchDto sdto, 
							HttpSession session,
							Model model) {
		log.info("boardList()");
		String view = bServ.getBoardList(sdto, session, model);
		
		return view;
	}
	
	@GetMapping("writeForm")
	public String writeForm() {
		log.info("writeForm()");
		
		return "writeForm";
	}
	
	@PostMapping("writeProc")
	public String writeProc(@RequestPart("files") List<MultipartFile> files, 
							BoardDto board,
							HttpSession session,
							RedirectAttributes rttr) {
		log.info("writeProc()");
		String view = bServ.boardWrite(files, board, session, rttr);
		return view;
	}
}//class end
