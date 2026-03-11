package com.example.trade.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.example.trade.dto.Board;
import com.example.trade.dto.Comment;
import com.example.trade.dto.ContractDelivery;
import com.example.trade.dto.DeliveryHistory;
import com.example.trade.dto.Notification;
import com.example.trade.dto.Order;
import com.example.trade.dto.Page;
import com.example.trade.dto.RewardHistory;

@Mapper
public interface AdminMapper {
	
	// 자주 묻는 질문(FAQ) 목록
	List<Map<String, Object>> selectFAQList();
	
	// 자주 묻는 질문(FAQ) 전체 행 수 조회
	int selectFAQTotalCount(Page page);
	
	// 자주 묻는 질문(FAQ) 상세 조회
	Board selectFAQOne(Board board);
	
	// 자주 묻는 질문(FAQ) 등록
	int insertBoard(Board board);
	
	// FAQ 수정
	int updateBoard(Board board);
	
	// FAQ 삭제
	int deleteBoard(Board board);

	// QNA 목록 조회
	List<Map<String, Object>> selectQNAListById();

	// QNA 목록 전체 행 수 조회
	int selectQNATotalCount(Page page);
	
	// 문의 내역 상세 조회
	List<Map<String, Object>> selectQNAOne(Board board);
	
	// 댓글 조회
	List<Map<String, Object>> selectCommentByBoardNo(int boardNo);
	
	// 댓글 등록
	int insertComment(Comment comment);
	
	// 댓글 수정
	int updateComment(Comment comment);
	
	// 댓글 삭제
	int deleteComment(Comment comment);
	
	// 공지사항 목록 조회
	List<Map<String, Object>> selectNoticeList();

	// 공지사항 전체 행 수 조회
	int selectNoticeTotalCount(Page page);
	
	// 공지사항 상세 조회
	Board selectNoticeOne(Board board);
	
	// 공지사항 등록
	int insertNotice(Board board);
	
	// 공지사항 수정
	int updateNotice(Board board);
	
	// 공지사항 삭제
	int deleteNotice(Board board);
	
	// 로그인 이력 조회
	List<Map<String, Object>> selectLoginHistory();

	// 알림 목록 조회
	List<Map<String, Object>> selectAlarmList();

	// 기업 회원의 배송 현황 조회
	List<Map<String, Object>> selectBizDeliveryList();

	// 개인 회원 배송 현황 조회
	List<Map<String, Object>> selectPersonalDeliveryList();

	// 개인 회원 배송 전체 행 수 조회
	int selectPersonalDeliveryTotalCount(Page page);

	// 개인 회원 배송 상태 변경
	int updatePersonalDelivery(Order order);
	
	// 기존 배송 이력 조회
	DeliveryHistory getDeliveryHistory(DeliveryHistory deliveryHistory);
	
	// 배송 이력 저장
	int insertDeliveryHistory(DeliveryHistory deliveryHistory);

	// 기존 주문 데이터 조회
	Order getOrderDetail(Order order);

	// 해당 주문의 다음 sub_order_no 계산
	String getNextSubOrderNo(String orderNo);
	
	// 교환 주문 행 생성
	int insertExchangeOrder(Order order);

	// 교환 완료 처리
	int updateExchangeComplete(Order order);

	// 교환 거절 처리
	int updateExchangeReject(Order order);

	// 반품 완료 처리
	int updateReturnComplete(Order order);

	// 반품 거절 처리
	int updateReturnReject(Order order);

	// 기업 회원 배송 처리
	int insertContractDelivery(ContractDelivery contractDelivery);

	// 기업 회원 배송 이력 등록
	int insertBizDeliveryHistory(DeliveryHistory deliveryHistory);

	// 기존 배송 이력 조회(기업)
	DeliveryHistory getBizDeliveryHistory(DeliveryHistory deliveryHistory);

	// 기업 회원 배송 상태 변경
	int updateBizDelivery(ContractDelivery contractDelivery);
	
	// 로그인 이력 저장
	int insertLoginHistory(String userId);

	// 주문 시 사용한 적립금 조회
	Integer getRewardUseByOrder(Order order);
	
	// 해당 주문의 전체 결제 금액 조회
	int getTotalAmountByOrder(Order order);

	// 해당 주문의 반품 금액 조회
	int getReturnAmountByOrder(Order order);

	// 반품 시 적립금 원복
	int refundReward(RewardHistory rewardHistory);

	// 미응답 QNA 수 조회
	int selectNoCommentQnaCount();

	// 배송 출발 시 알림 등록
	int insertNotification(Notification noti);

	// 배송 처리 후 기업 회원의 주문 조회
	Map<String, Object> getBizDeliveryInfo(int contractDeliveryNo);

	// 전체 회원 목록 조회
	List<String> getAllUserIds();

	// 기업 회원 배송 건 현재 상태 + version 조회
	ContractDelivery selectBizDeliveryById(int contractDeliveryNo);

	// 낙관적 락 기반 배송 완료로 상태 변경
	int updateBizDeliveryCompleteWithVersion(ContractDelivery contractDelivery);
}