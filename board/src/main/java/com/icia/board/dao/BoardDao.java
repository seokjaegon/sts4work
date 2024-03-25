package com.icia.board.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.icia.board.dto.BoardDto;
import com.icia.board.dto.SearchDto;

@Mapper
public interface BoardDao {
	//게시글 목록 가져오는 메소드
	List<BoardDto> selectBoardList(SearchDto sdto);
	//전체 게시글 개수 구하는 메소드
	int selectBoardCnt(SearchDto sdto);
	//게시글 내용 저장 메소드
	void insertBoard(BoardDto board);
}
