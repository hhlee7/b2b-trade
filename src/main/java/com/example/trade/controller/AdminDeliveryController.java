package com.example.trade.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.trade.dto.ContractDelivery;
import com.example.trade.dto.DeliveryHistory;
import com.example.trade.dto.Order;
import com.example.trade.service.AdminDeliveryService;

@Controller
public class AdminDeliveryController {
	private AdminDeliveryService adminDeliveryService;
	public AdminDeliveryController(AdminDeliveryService adminDeliveryService) {
		this.adminDeliveryService = adminDeliveryService;
	}
	
	// 기업 회원의 배송 현황 페이지
	@GetMapping("/admin/bizDeliveryList")
	public String bizDeliveryList(Model model) {
		List<Map<String, Object>> bizDeliveryList = adminDeliveryService.getBizDeliveryList();
		model.addAttribute("bizDeliveryList", bizDeliveryList);
		return "admin/bizDeliveryList";
	}
	
	// 개인 회원 배송 현황 페이지
	@GetMapping("/admin/personalDeliveryList")
	public String personalDeliveryList(Model model) {
		
		// 개인 회원 배송 현황 조회
		List<Map<String, Object>> personalDeliveryList = adminDeliveryService.getPersonalDeliveryList();
		
		// 모델에 값 전달
		model.addAttribute("personalDeliveryList", personalDeliveryList);
		
		return "admin/personalDeliveryList";
	}
	
	// 개인 회원 배송 처리 페이지
    @GetMapping("/admin/personalDeliveryUpdate")
    public String personalDeliveryUpdate(@RequestParam String orderNo,
	                                     @RequestParam String subOrderNo,
	                                     Model model) {
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("subOrderNo", subOrderNo);
        return "admin/personalDeliveryUpdate";
    }

    // 개인 회원 배송 처리
    @PostMapping("/admin/personalDeliveryUpdate")
    public String personalDeliveryUpdate(Order order, DeliveryHistory deliveryHistory, Principal principal) {
    	
    	String updateUser = principal.getName();
    	order.setUpdateUser(updateUser);
    	
    	adminDeliveryService.updatePersonalDelivery(order, deliveryHistory);
    	
        return "redirect:/admin/personalDeliveryList";
    }
    
    // 개인 회원 배송 완료 처리
	@PostMapping("/admin/personalDeliveryComplete")
	public String personalDeliveryComplete(Order order, DeliveryHistory deliveryHistory, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateDeliveryComplete(order, deliveryHistory);
		
		return "redirect:/admin/personalDeliveryList";
	}
    
    // 교환 배송 처리 페이지
    @GetMapping("/admin/personalExchangeUpdate")
    public String personalExchangeUpdate(Order order, Model model) {
    	model.addAttribute("order", order);
    	return "admin/personalExchangeUpdate";
    }
    
    // 교환 승인 처리
	@PostMapping("/admin/exchangeApprove")
	public String exchangeApprove(Order order, DeliveryHistory deliveryHistory, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateExchangeApprove(order, deliveryHistory);
		return "redirect:/admin/personalDeliveryList";
	}
	
	// 교환 완료 처리
	@PostMapping("/admin/exchangeComplete")
	public String exchangeComplete(Order order, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateExchangeComplete(order);
		return "redirect:/admin/personalDeliveryList";
	}

	// 교환 거절 처리
	@PostMapping("/admin/exchangeReject")
	public String exchangeReject(Order order, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateExchangeReject(order);
		return "redirect:/admin/personalDeliveryList";
	}

	// 반품 승인 처리
	@PostMapping("/admin/returnApprove")
	public String returnApprove(Order order, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateReturnApprove(order);
		return "redirect:/admin/personalDeliveryList";
	}
	
	// 반품 완료 처리
	@PostMapping("/admin/returnComplete")
	public String returnComplete(Order order, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateReturnComplete(order);
		return "redirect:/admin/personalDeliveryList";
	}

	// 반품 거절 처리
	@PostMapping("/admin/returnReject")
	public String returnReject(Order order, Principal principal) {
		
		String updateUser = principal.getName();
		order.setUpdateUser(updateUser);
		
		adminDeliveryService.updateReturnReject(order);
		return "redirect:/admin/personalDeliveryList";
	}
	
	// 기업 회원 배송 처리 페이지
    @GetMapping("/admin/bizDeliveryUpdate")
    public String bizDeliveryUpdate(@RequestParam int containerNo, Model model) {
        model.addAttribute("containerNo", containerNo);
        return "admin/bizDeliveryUpdate";
    }

    // 기업 회원 배송 처리
    @PostMapping("/admin/bizDeliveryUpdate")
    public String bizDeliveryUpdate(ContractDelivery contractDelivery, DeliveryHistory deliveryHistory) {
    	
    	adminDeliveryService.insertBizDelivery(contractDelivery, deliveryHistory);
    	
        return "redirect:/admin/bizDeliveryList";
    }
    
    // 기업 회원 배송 완료 처리
	@PostMapping("/admin/bizDeliveryComplete")
	public String bizDeliveryComplete(ContractDelivery contractDelivery, DeliveryHistory deliveryHistory) {
		
		adminDeliveryService.bizDeliveryComplete(contractDelivery, deliveryHistory);
		
		return "redirect:/admin/bizDeliveryList";
	}
}
