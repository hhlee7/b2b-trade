package com.example.trade.service;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.trade.dto.ContractDelivery;
import com.example.trade.dto.DeliveryHistory;
import com.example.trade.dto.Notification;
import com.example.trade.dto.Order;
import com.example.trade.dto.Page;
import com.example.trade.dto.RewardHistory;
import com.example.trade.mapper.AdminMapper;

@Service
public class AdminDeliveryService {
	private AdminMapper adminMapper;
	public AdminDeliveryService(AdminMapper adminMapper) {
		this.adminMapper = adminMapper;
	}
	
	// 기업 회원의 배송 현황 조회
	public List<Map<String, Object>> getBizDeliveryList() {
		return adminMapper.selectBizDeliveryList();
	}

	// 개인 회원 배송 현황 조회
	public List<Map<String, Object>> getPersonalDeliveryList() {
		return adminMapper.selectPersonalDeliveryList();
	}

	// 개인 회원 배송 전체 행 수 조회
	public int getPersonalDeliveryTotalCount(Page page) {
		return adminMapper.selectPersonalDeliveryTotalCount(page);
	}
	
	// 개인 회원 배송 처리
	@Transactional
	public int updatePersonalDelivery(Order order, DeliveryHistory deliveryHistory) {
		order.setDeliveryStatus("DS002"); // 배송중 처리
    	deliveryHistory.setDeliveryStatus("DS002"); // 배송중 처리
		adminMapper.updatePersonalDelivery(order);
		
		// 기존 주문 조회
		Order dbOrder = adminMapper.getOrderDetail(order);
		
		// 알림 발송 (배송 출발 알림)
		Notification noti = new Notification();
		noti.setTargetType("USER"); // 대상 타입
		noti.setTargetValue(dbOrder.getUserId()); // 주문자 ID
		noti.setNotificationType("NC002"); // 알림 유형(배송)
		noti.setNotificationTitle("배송 출발");
		noti.setNotificationContent(dbOrder.getProductName() + " 상품의 배송을 시작했습니다.");
		noti.setTargetUrl("/personal/orderOne?orderNo=" + order.getOrderNo()); // 클릭 시 이동할 URL
		noti.setImageUrl(null); // 필요 시 썸네일 등
		noti.setCreateUser("system"); // 시스템 발송 처리

		adminMapper.insertNotification(noti);
		
		return adminMapper.insertDeliveryHistory(deliveryHistory);
	}
	
	// 개인 회원 배송 완료 처리
	@Transactional
	public int updateDeliveryComplete(Order order, DeliveryHistory deliveryHistory) {
		
		// 기존 배송 이력 조회
		DeliveryHistory newDeliveryHistory = adminMapper.getDeliveryHistory(deliveryHistory);
		
		order.setDeliveryStatus("DS003"); // 배송완료 처리
		deliveryHistory.setDeliveryCompany(newDeliveryHistory.getDeliveryCompany()); // 택배사
		deliveryHistory.setTrackingNo(newDeliveryHistory.getTrackingNo()); // 운송장 번호
    	deliveryHistory.setDeliveryStatus("DS003"); // 배송완료 처리
		adminMapper.updatePersonalDelivery(order);
		return adminMapper.insertDeliveryHistory(deliveryHistory);
	}

	// 교환 승인 처리
	@Transactional
	public int updateExchangeApprove(Order order, DeliveryHistory deliveryHistory) {
		
		// 기존 주문 건 배송 상태 교환중으로 변경
		order.setDeliveryStatus("DS007");
		adminMapper.updatePersonalDelivery(order);
		
		// 기존 주문 조회
		Order newOrder = adminMapper.getOrderDetail(order);

		// parent_sub_order_no 설정
		newOrder.setParentSubOrderNo(order.getSubOrderNo()); // 기존 subOrderNo를 parent로 설정

		// 새로운 sub_order_no 설정
		String newSubOrderNo = adminMapper.getNextSubOrderNo(order.getOrderNo());
		newOrder.setSubOrderNo(newSubOrderNo);
	    
		// 배송 이력 데이터 설정
		newOrder.setOrderStatus("OS002"); // 주문완료
		newOrder.setDeliveryStatus("DS002"); // 배송중
		newOrder.setCreateUser(order.getUpdateUser()); // 현재 관리자
		newOrder.setUseStatus("Y");
		
		// 새 교환 행 order 테이블에 insert
		adminMapper.insertExchangeOrder(newOrder);
		
		// 배송 이력에도 새로운 sub_order_no 로 입력
		deliveryHistory.setSubOrderNo(newSubOrderNo);
		deliveryHistory.setDeliveryStatus("DS002"); // 배송중
		
		// 새 배송 이력 delivery_history 테이블에 insert
		return adminMapper.insertDeliveryHistory(deliveryHistory);
	}
	
	// 교환 완료 처리
	@Transactional
	public int updateExchangeComplete(Order order) {
		order.setDeliveryStatus("DS008"); // 교환완료
		return adminMapper.updateExchangeComplete(order);
	}

	// 교환 거절 처리
	@Transactional
	public int updateExchangeReject(Order order) {
		order.setDeliveryStatus("DS003"); // 배송완료 복귀
		order.setExchangeQuantity(0); // 수량 0 복귀
		order.setExchangeReason(null); // 사유 null 복귀
		order.setExchangeRequestTime(null); // 신청일 null 복귀
		return adminMapper.updateExchangeReject(order);
	}

	// 반품 승인 처리
	@Transactional
	public int updateReturnApprove(Order order) {
		order.setDeliveryStatus("DS010"); // 반품중
		return adminMapper.updatePersonalDelivery(order);
	}

	// 반품 완료 처리
	@Transactional
	public int updateReturnComplete(Order order) {
		
		int totalAmount = adminMapper.getTotalAmountByOrder(order); // 전체 결제 금액
		int returnAmount = adminMapper.getReturnAmountByOrder(order); // 반품 상품 금액
		// 사용한 적립금
	    // null-safe 처리
	    Integer rewardUseVal = adminMapper.getRewardUseByOrder(order);
	    int rewardUse = (rewardUseVal != null) ? rewardUseVal : 0;
		
		// 반품 제외 구매 금액
		int remainAmount = totalAmount - returnAmount;
		
		// 반품 시 적립금 규칙 적용
		// 사용 적립금이 반품 제외 금액의 10% 초과 -> 적립금 원복 + 차액 현금 환불
		if(rewardUse > remainAmount * 0.1) {
			RewardHistory rewardHistory = new RewardHistory();
			rewardHistory.setOrderNo(order.getOrderNo());
			rewardHistory.setRewardUse(-rewardUse); // 음수 insert
			adminMapper.refundReward(rewardHistory);
		}
		
		order.setDeliveryStatus("DS005"); // 반품완료로 상태 변경
		return adminMapper.updateReturnComplete(order);
	}

	// 반품 거절 처리
	@Transactional
	public int updateReturnReject(Order order) {
		order.setDeliveryStatus("DS003"); // 배송완료 복귀
		order.setReturnQuantity(0); // 수량 0 복귀
		order.setReturnReason(null); // 사유 null 복귀
		order.setReturnRequestTime(null); // 신청일 null 복귀
		return adminMapper.updateReturnReject(order);
	}

	// 기업 회원 배송 처리
	@Transactional
	public int insertBizDelivery(ContractDelivery contractDelivery, DeliveryHistory deliveryHistory) {
		contractDelivery.setContractDeliveryStatus("DS002"); // 배송중 처리
		
		try {
			adminMapper.insertContractDelivery(contractDelivery);
		} catch (DuplicateKeyException e) {
			return 0; // 이미 동일 containerNo로 배송 처리된 요청인 경우
		}
		
		// 생성된 contract_delivery_no를 deliveryHistory에 세팅
		deliveryHistory.setContractDeliveryNo(contractDelivery.getContractDeliveryNo());
		deliveryHistory.setDeliveryStatus("DS002"); // 배송중 처리
		
		// 기업회원 배송 관련 정보 조회
		Map<String, Object> bizOrder = adminMapper.getBizDeliveryInfo(contractDelivery.getContractDeliveryNo());
		
		// 배송 시작 시 알림 생성
		Notification noti = new Notification();
		noti.setTargetType("USER");
		noti.setTargetValue((String) bizOrder.get("userId")); // 기업 담당자 ID
		noti.setNotificationType("NC002"); // 배송 알림
		noti.setNotificationTitle("배송 출발");
		noti.setNotificationContent("계약번호 " + bizOrder.get("contractNo") + "번 상품의 배송을 시작했습니다.");
		noti.setTargetUrl("/biz/deliveryList");
		noti.setImageUrl(null); // 필요 시 썸네일 등
		noti.setCreateUser("system");
		
		adminMapper.insertNotification(noti);
		
		return adminMapper.insertBizDeliveryHistory(deliveryHistory);
	}
	
	// 기업 회원 배송 완료 처리
	@Transactional
	public int bizDeliveryComplete(ContractDelivery contractDelivery, DeliveryHistory deliveryHistory) {
		// 1. 현재 배송 정보 조회
		ContractDelivery currentDelivery = adminMapper.selectBizDeliveryById(contractDelivery.getContractDeliveryNo());

		// 배송 건이 없으면 종료
		if(currentDelivery == null) {
			return 0;
		}
		
		// 2. 배송중 상태(DS002)인 경우에만 완료 처리 가능
		if (!"DS002".equals(currentDelivery.getContractDeliveryStatus())) {
			return 0;
		}
		
		// 3. 배송 상태 변경 정보 세팅
		contractDelivery.setContractDeliveryStatus("DS003"); // 배송완료
		contractDelivery.setVersion(currentDelivery.getVersion());
		
		// 4. 낙관적 락 기반 배송 상태 update 시도
		int updatedRow = adminMapper.updateBizDeliveryCompleteWithVersion(contractDelivery);
		
		// 다른 요청이 먼저 처리한 경우 종료
		if(updatedRow == 0) {
			return 0;
		}
		
		// 5. update 성공한 요청만 기존 배송 이력 조회(택배사/운송장번호 복사)
		DeliveryHistory oldDeliveryHistory = adminMapper.getBizDeliveryHistory(deliveryHistory);
		if(oldDeliveryHistory == null) {
			return 0;
		}
		
		// 6. 배송 완료 이력 insert 수행
		deliveryHistory.setDeliveryCompany(oldDeliveryHistory.getDeliveryCompany()); // 택배사
		deliveryHistory.setTrackingNo(oldDeliveryHistory.getTrackingNo()); // 운송장 번호
    	deliveryHistory.setDeliveryStatus("DS003"); // 배송완료
    	
    	return adminMapper.insertBizDeliveryHistory(deliveryHistory);
	}
}
