package com.icia.board.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.icia.board.dao.BoardDao;
import com.icia.board.dao.MemberDao;
import com.icia.board.dto.BoardDto;
import com.icia.board.dto.BoardFileDto;
import com.icia.board.dto.MemberDto;
import com.icia.board.dto.ReplyDto;
import com.icia.board.dto.SearchDto;
import com.icia.board.util.PagingUtil;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BoardService {
	@Autowired
	private BoardDao bDao;
	@Autowired
	private MemberDao mDao;//회원 point 정보 변경에 사용
	
	//transaction 관련
	@Autowired
	private PlatformTransactionManager manager;
	@Autowired
	private TransactionDefinition definition;
	
	private int lcnt = 10;//한 화면(페이지)에 보여질 게시글 개수
	
	public String getBoardList(SearchDto sdto, 
							   HttpSession session,
							   Model model) {
		log.info("getBoardList()");
		String view = "boardList";
		
		//DB에서 게시글 목록 가져오기
		int num = sdto.getPageNum();
		
		if(sdto.getListCnt() == 0) {
			sdto.setListCnt(lcnt);//목록 값 설정(초기 10개)
		}
		
		sdto.setPageNum((num-1) * sdto.getListCnt());
		List<BoardDto> bList = bDao.selectBoardList(sdto);
		model.addAttribute("bList", bList);
		
		//페이징 처리
		sdto.setPageNum(num);
		String pageHtml = getPaging(sdto);
		model.addAttribute("paging", pageHtml);
		
		//페이지 관련 내용 세션에 저장
		if(sdto.getColname() != null) {
			session.setAttribute("sdto", sdto);
		} else {
			session.removeAttribute("sdto");//검색을 안한 목록을 위해 삭제.
		}
		//별개로 페이지 번호도 저장
		session.setAttribute("pageNum", num);
		
		return view;
	}

	private String getPaging(SearchDto sdto) {
		log.info("getPaging()");
		String pageHtml = null;
		
		//전체 게시글 개수
		int maxNum = bDao.selectBoardCnt(sdto);
		
		int pageCnt = 3;//페이지에서 보여질 페이지번호 개수
		
		String listName = "boardList?";
		if(sdto.getColname() != null) {
			//검색 기능을 사용한 경우
			listName += "colname=" + sdto.getColname()
					 + "&keyword=" + sdto.getKeyword() + "&";
			//<a href='/boardList?colname=b_title&keyword=3&pageNum=...'>
		}
		
		PagingUtil paging = new PagingUtil(maxNum, 
							sdto.getPageNum(),
							sdto.getListCnt(),
							pageCnt,
							listName);
		
		pageHtml = paging.makePaging();
		
		return pageHtml;
	}
	
	//게시글, 파일 저장 및 회원 정보(point) 변경
	public String boardWrite(List<MultipartFile> files, 
							 BoardDto board,
							 HttpSession session,
							 RedirectAttributes rttr) {
		log.info("boardWrite()");
		
		//트랜젝션 상태 처리 객체
		TransactionStatus status = manager.getTransaction(definition);
		
		String view = null;
		String msg = null;
		
		try {
			//게시글 저장
			bDao.insertBoard(board);
			log.info("b_num : {}", board.getB_num());
			
			//파일 저장
			if(!files.get(0).isEmpty()) {//업로드 파일이 있다면
				fileUpload(files, session, board.getB_num());
			}
			//작성자의 point 수정
			MemberDto member = (MemberDto) session.getAttribute("member");
			int point = member.getM_point() + 10;
			if(point > 100) {
				point = 100;
			}
			
			member.setM_point(point);
			mDao.updateMemberPoint(member);
			
			//세션에 새 정보를 저장.
			member = mDao.selectMember(member.getM_id());
			session.setAttribute("member", member);
			
			//commit 수행
			manager.commit(status);
			
			view = "redirect:boardList?pageNum=1";
			msg = "작성 성공";
		} catch (Exception e) {
			e.printStackTrace();
			//rollback 수행
			manager.rollback(status);
			view = "redirect:writeForm";
			msg = "작성 실패";
		}
		
		rttr.addFlashAttribute("msg", msg);
		
		return view;
	}

	private void fileUpload(List<MultipartFile> files, 
							HttpSession session, 
							int b_num) throws Exception {
		//파일 저장 실패 시 데이터베이스 롤백작업이 이루어지도록 예외를 throws 할 것.
		log.info("fileUpload()");
		
		//파일 저장 위치 처리(session에서 저장 경로를 구함)
		String realPath = session.getServletContext().getRealPath("/");
		log.info("realPath : {}", realPath);
		
		realPath += "upload/";//파일 업로드 폴더
		
		File folder = new File(realPath);
		if(folder.isDirectory() == false) {
			folder.mkdir();//폴더 생성 메소드
		}
		
		for(MultipartFile mf : files) {
			//파일명 추출
			String oriname = mf.getOriginalFilename();
			
			BoardFileDto bfd = new BoardFileDto();
			bfd.setBf_oriname(oriname);
			bfd.setBf_bnum(b_num);
			String sysname = System.currentTimeMillis()
					+ oriname.substring(oriname.lastIndexOf("."));
			//확장자 : 파일을 구분하기 위한 식별 체계. (예. xxxx.jpg)
			bfd.setBf_sysname(sysname);
			
			//파일 저장
			File file = new File(realPath + sysname);
			mf.transferTo(file);
			
			//파일 정보 저장
			bDao.insertFile(bfd);
		}
	}

	public String getBoard(int b_num, Model model) {
		log.info("getBoard()");
		
		//조회수 업데이트 부분은 과제
		
		//게시글 번호(b_num)로 게시물 가져오기
		BoardDto board = bDao.selectBoard(b_num);
		model.addAttribute("board", board);
		
		//파일 목록 가져오기
		List<BoardFileDto> bfList = bDao.selectFileList(b_num);
		model.addAttribute("bfList", bfList);
		
		//댓글 목록 가져오기
		List<ReplyDto> rList = bDao.selectReplyList(b_num);
		model.addAttribute("rList", rList);
		
		return "boardDetail";
	}

	public ReplyDto replyInsert(ReplyDto reply) {
		log.info("replyInsert()");
		
		TransactionStatus status = manager.getTransaction(definition);
		
		try {
			bDao.insertReply(reply);
			reply = bDao.selectReply(reply.getR_num());
			
			manager.commit(status);
		}catch (Exception e) {
			e.printStackTrace();
			manager.rollback(status);
			reply = null;
		}
		return reply;
	}

	public ResponseEntity<Resource> fileDownload(BoardFileDto bfile, 
												 HttpSession session) 
												 throws IOException {
		log.info("fileDownload()");
		String realPath = session.getServletContext().getRealPath("/");
		realPath += "upload/" + bfile.getBf_sysname();
		
		//실제 하드디스크에 저장된 파일과 연결하는 객체를 생성.
		InputStreamResource fResource = 
				new InputStreamResource(new FileInputStream(realPath));
		
		//파일명이 한글인 경우 인코딩 처리가 필요.(UTF-8)
		String fileName = URLEncoder.encode(bfile.getBf_oriname(), "UTF-8");
		
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_OCTET_STREAM)
				.cacheControl(CacheControl.noCache())
				.header(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=" + fileName)
				.body(fResource);
	}
	
}//class end
