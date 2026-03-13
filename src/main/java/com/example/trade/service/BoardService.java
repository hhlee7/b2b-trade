package com.example.trade.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.trade.dto.Board;
import com.example.trade.dto.Comment;
import com.example.trade.dto.Page;
import com.example.trade.mapper.BoardMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BoardService {
	private BoardMapper boardMapper;
	private NoticeCacheService noticeCacheService;
	public BoardService(BoardMapper boardMapper, NoticeCacheService noticeCacheService) {
		this.boardMapper = boardMapper;
		this.noticeCacheService = noticeCacheService;
	}

	// 자주 묻는 질문(FAQ) 목록
	public List<Map<String, Object>> getFAQList(Page page) {
		return boardMapper.selectFAQList(page);
	}
	
	// FAQ 전체 행 수 조회
	public int getFAQTotalCount(Page page) {
		return boardMapper.selectFAQTotalCount(page);
	}

	// 접속한 사용자의 문의 내역
	public List<Map<String, Object>> getQNAList(Page page) {
		return boardMapper.selectQNAListById(page);
	}
	
	// 문의 내역 전체 행 수 조회
	public int getQNATotalCount(Page page) {
		return boardMapper.selectQNATotalCount(page);
	}

	// 문의 내역 상세 조회
	public List<Map<String, Object>> getQNAOne(Board board) {
		return boardMapper.selectQNAOne(board);
	}
	
	// 1:1 문의 등록
	public int insertBoard(Board board) {
		return boardMapper.insertBoard(board);
	}
	
	// 1:1 문의 수정
	public int updateQNA(Board board) {
		return boardMapper.updateQNA(board);
	}
	
	// 1:1 문의 삭제(비활성화)
	public int deleteQNA(Board board) {
		return boardMapper.deleteQNA(board);
	}
	
	// 댓글 조회
	public List<Map<String, Object>> getCommentByBoardNo(int boardNo) {
		return boardMapper.selectCommentByBoardNo(boardNo);
	}
	
	// 댓글 등록
	public int insertComment(Comment comment) {
		return boardMapper.insertComment(comment);
	}
	
	// 댓글 수정
	public int updateComment(Comment comment) {
		return boardMapper.updateComment(comment);
	}
	
	// 댓글 삭제
	public int deleteComment(Comment comment) {
		return boardMapper.deleteComment(comment);
	}
	
	// 공지사항 목록 조회
	public List<Map<String, Object>> getNoticeList(Page page) {
		return boardMapper.selectNoticeList(page);
	}
	
	// 공지사항 전체 행 수 조회
	public int getNoticeTotalCount(Page page) {
		return boardMapper.selectNoticeTotalCount(page);
	}

	// 공지사항 상세 조회
	public List<Map<String, Object>> getNoticeOne(int boardNo) {
		// 매 요청마다 조회수 증가
		boardMapper.updateBoardViewCount(boardNo);
		// 상세 데이터는 캐시 사용
		return noticeCacheService.getNoticeOne(boardNo);
	}
}
