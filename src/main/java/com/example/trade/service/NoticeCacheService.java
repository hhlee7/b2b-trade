package com.example.trade.service;

import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.example.trade.mapper.BoardMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class NoticeCacheService {
	private BoardMapper boardMapper;
	public NoticeCacheService(BoardMapper boardMapper) {
		this.boardMapper = boardMapper;
	}

	// 공지사항 상세 캐시 조회
	@Cacheable(value = "noticeOne", key = "#boardNo")
	public List<Map<String, Object>> getNoticeOne(int boardNo) {
		log.info("공지사항 상세 DB 조회 실행 - boardNo={}", boardNo);
		return boardMapper.selectNoticeOne(boardNo);
	}

	// 특정 공지 캐시 제거
	@CacheEvict(value = "noticeOne", key = "#boardNo")
	public void evictNoticeOne(int boardNo) {
		log.info("공지사항 상세 캐시 삭제 - boardNo={}", boardNo);
	}
}
