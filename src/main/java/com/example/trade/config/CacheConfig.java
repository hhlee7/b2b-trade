package com.example.trade.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {
	
	@Bean
	public CacheManager cacheManager() {
		// Caffeine 기반 캐시 매니저 생성
		// "noticeOne"은 캐시 이름 / 이후 @Cacheable(value = "noticeOne")과 같은 방식으로 사용
		CaffeineCacheManager cacheManager = new CaffeineCacheManager("noticeOne");
		
		cacheManager.setCaffeine(
			Caffeine.newBuilder()
					.expireAfterWrite(10, TimeUnit.MINUTES) // 캐시에 저장된 데이터가 생성(or 갱신)된 시점부터 10분이 지나면 자동으로 만료 설정
					.maximumSize(1000) // 캐시 최대 저장 개수 1000개 제한 / 1000개를 초과하면 오래되거나 덜 사용된 데이터부터 제거됨
		);
		
		return cacheManager; // 설정 적용된 CacheManager를 Spring 컨테이너에 반환
	}
}
