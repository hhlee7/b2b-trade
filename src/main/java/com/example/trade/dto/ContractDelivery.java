package com.example.trade.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContractDelivery {
	private int contractDeliveryNo;
	private int addressNo;
	private LocalDateTime contractDeliveryTime;
	private String contractDeliveryStatus;
	private int containerNo;
	private int version;
}
