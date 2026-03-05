package com.example.trade.service;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trade.dto.Board;
import com.example.trade.dto.Comment;
import com.example.trade.dto.ContractDelivery;
import com.example.trade.dto.DeliveryHistory;
import com.example.trade.dto.Notification;
import com.example.trade.dto.Order;
import com.example.trade.dto.Page;
import com.example.trade.dto.RewardHistory;
import com.example.trade.mapper.AdminMapper;

@Service
public class AdminService {
	private AdminMapper adminMapper;
	public AdminService(AdminMapper adminMapper) {
		this.adminMapper = adminMapper;
	}
	
	// 자주 묻는 질문(FAQ) 목록
	public List<Map<String, Object>> getFAQList() {
		return adminMapper.selectFAQList();
	}
	
	// 자주 묻는 질문(FAQ) 전체 행 수 조회
	public int getFAQTotalCount(Page page) {
		return adminMapper.selectFAQTotalCount(page);
	}
	
	// 자주 묻는 질문(FAQ) 상세 조회
	public Board getFAQOne(Board board) {
		return adminMapper.selectFAQOne(board);
	}
	
	// 자주 묻는 질문(FAQ) 등록
	public int insertBoard(Board board) {
		board.setBoardCode("BC001"); // 공통 코드 FAQ로 세팅
		return adminMapper.insertBoard(board);
	}
	
	// FAQ 수정
	public int updateFAQ(Board board) {
		return adminMapper.updateBoard(board);
	}

	// FAQ 삭제
	public int deleteFAQ(Board board) {
		return adminMapper.deleteBoard(board);
	}
	
	// QNA 목록 조회
	public List<Map<String, Object>> getQNAList() {
		return adminMapper.selectQNAListById();
	}
	
	// QNA 목록 전체 행 수 조회
	public int getQNATotalCount(Page page) {
		return adminMapper.selectQNATotalCount(page);
	}
	
	// QNA 상세 조회
	public List<Map<String, Object>> getQNAOne(Board board) {
		return adminMapper.selectQNAOne(board);
	}
	
	// 댓글 조회
	public List<Map<String, Object>> getCommentByBoardNo(int boardNo) {
		return adminMapper.selectCommentByBoardNo(boardNo);
	}
	
	// 댓글 등록
	public int insertComment(Comment comment) {
		return adminMapper.insertComment(comment);
	}
	
	// 댓글 수정
	public int updateComment(Comment comment) {
		return adminMapper.updateComment(comment);
	}
	
	// 댓글 삭제
	public int deleteComment(Comment comment) {
		return adminMapper.deleteComment(comment);
	}

	// 공지사항 목록 조회
	public List<Map<String, Object>> getNoticeList() {
		return adminMapper.selectNoticeList();
	}
	
	// 공지사항 전체 행 수 조회
	public int getNoticeTotalCount(Page page) {
		return adminMapper.selectNoticeTotalCount(page);
	}
	
	// 공지사항 상세 조회
	public Board getNoticeOne(Board board) {
		return adminMapper.selectNoticeOne(board);
	}
	
	// 공지사항 등록
	public int insertNotice(Board board, Principal principal) {
		String username = principal.getName(); // 로그인한 관리자
		board.setCreateUser(username);
		board.setBoardCode("BC003"); // 공통 코드 공지사항으로 세팅
		
		// 공지 등록
		int result = adminMapper.insertNotice(board);

		if(result > 0) {
			// 전체 회원 목록 조회
			List<String> userIds = adminMapper.getAllUserIds();
			// 회원별 알림 생성
			for(String userId : userIds) {
				Notification noti = new Notification();
				noti.setTargetType("USER"); // 개별 회원 대상
				noti.setTargetValue(userId); // 대상 회원 ID
				noti.setNotificationType("NC005"); // 알림 유형: 공지
				noti.setNotificationTitle(board.getBoardTitle());
				noti.setNotificationContent("새로운 공지사항을 확인하세요.");
				noti.setTargetUrl("/public/noticeOne?boardNo=" + board.getBoardNo());
				noti.setImageUrl(null); // 필요 시 썸네일 등
				noti.setCreateUser("system");
				
				adminMapper.insertNotification(noti);
			}
		}
		return result;
	}
	
	// 공지사항 수정
	public int updateNotice(Board board) {
		return adminMapper.updateNotice(board);
	}
	
	// 공지사항 삭제
	public int deleteNotice(Board board) {
		return adminMapper.deleteNotice(board);
	}
	
	// 로그인 이력 조회
	public List<Map<String, Object>> getLoginHistory() {
		return adminMapper.selectLoginHistory();
	}

	// 알림 목록 조회
	public List<Map<String, Object>> getAlarmHistory() {
		return adminMapper.selectAlarmList();
	}

	// 로그인 이력 저장
	public int saveLoginHistory(String userId) {
		return adminMapper.insertLoginHistory(userId);
	}

	// 미응답 QNA 수 조회
	public int getNoCommentQnaCount() {
		return adminMapper.selectNoCommentQnaCount();
	}
}
