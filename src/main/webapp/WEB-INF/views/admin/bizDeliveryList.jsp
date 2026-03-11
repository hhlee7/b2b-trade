<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<%@ include file="/WEB-INF/common/head.jsp"%>
<title>배송 관리(기업)</title>
<!-- font -->
<link href="https://cdn.jsdelivr.net/gh/sunn-us/SUIT/fonts/static/woff2/SUIT.css" rel="stylesheet">
<!-- Bootstrap5 + DataTables CSS -->
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.datatables.net/1.13.8/css/dataTables.bootstrap5.min.css" rel="stylesheet">
<style>
	body {
		font-family: "SUIT", -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Apple SD Gothic Neo", "Noto Sans KR", "Malgun Gothic", Arial, sans-serif;
		background: #fff;
	}
	
	a {	text-decoration: none;
		color: inherit;
	}
	
	#table th #table td {
		text-align: center !important;
	}
	
	/* 테이블 헤더 가운데 정렬 */
	table.dataTable thead th {
		text-align: center !important;
		padding-right: 20px !important; /* 아이콘 공간 확보 */
		vertical-align: middle; /* 텍스트와 아이콘 세로 가운데 정렬 */
	}
	
	/* 정렬 아이콘 위치 조정 */
	table.dataTable thead .sorting:after,
	table.dataTable thead .sorting_asc:after,
	table.dataTable thead .sorting_desc:after {
		right: 5px;  /* 아이콘을 헤더 오른쪽 끝으로 이동 */
		left: auto;
	}
	
	/* 검색창 길이 설정 */
	.dataTables_filter input {
		width: 300px !important;
	}
	
	/* 현재 페이지(active) 버튼 */
	.dataTables_wrapper .dataTables_paginate .page-item.active .page-link {
		background-color: #000 !important; /* 검정 배경 */
		color: #fff !important;           /* 흰 글씨 */
		border-color: #000 !important;
	}

	/* 일반 페이지 버튼 (흰 배경 + 검정 글씨) */
	.dataTables_wrapper .dataTables_paginate .page-item .page-link {
		color: #000 !important;
		background-color: #fff !important;
	}

	/* hover 시 (일반 버튼만) */
	.dataTables_wrapper .dataTables_paginate .page-item:not(.active):not(.disabled) .page-link:hover {
		background-color: #f2f2f2 !important;
		color: #000 !important;
	}

	/* disabled 버튼 (이전/다음 비활성화 시) */
	.dataTables_wrapper .dataTables_paginate .page-item.disabled .page-link {
		color: #6c757d !important;        /* 글씨 회색 */
		background-color: #f8f9fa !important; /* 연한 회색 배경 */
		border-color: #dee2e6 !important; /* 연한 회색 테두리 */
		pointer-events: none;             /* 클릭 안되게 */
	}
</style>
</head>
<body>

<c:if test="${not empty msg}">
	<script>
		alert("${msg}");
	</script>
</c:if>

<!-- 공통 헤더 -->
<%@include file="/WEB-INF/common/header/header.jsp"%>

	<div class="container-xl py-3">
		<h1 class="h4 mb-3">배송 관리(기업 회원)</h1>
	
		<table id="table" class="table table-bordered table-hover datatable text-center nowrap">
			<thead class="table-light">
				<tr>
					<th>계약 번호</th>
					<th>계약금</th>
					<th>상태</th>
					<th>입금일시</th>
					<th>잔금</th>
					<th>상태</th>
					<th>입금일시</th>
					<th>배송지 주소</th>
					<th>배송 일시</th>
					<th>배송 상태</th>
					<th>배송 처리</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="bizDeliveryList" items="${bizDeliveryList}">
					<tr>
						<td><a href="/admin/contractOne?contractNo=${bizDeliveryList.contractNo}">${bizDeliveryList.contractNo}</a></td>
						<td>₩<fmt:formatNumber value="${bizDeliveryList.downPayment}" pattern="#,###"/></td>
						<td>${bizDeliveryList.downPaymentStatus}</td>
						<td>${bizDeliveryList.downPaymentDate}</td>
						<td>₩<fmt:formatNumber value="${bizDeliveryList.finalPayment}" pattern="#,###"/></td>
						<td>${bizDeliveryList.finalPaymentStatus}</td>
						<td>${bizDeliveryList.finalPaymentDate}</td>
						<td>${bizDeliveryList.address}</td>
						<td>${bizDeliveryList.contractDeliveryTime}</td>
						<td>${bizDeliveryList.contractDeliveryStatus}</td>
						<td>
							<c:choose>
								<%-- 배송 처리 : null일 때만 가능 --%>
								<c:when test="${bizDeliveryList.contractDeliveryStatus eq null}">
									<form action="/admin/bizDeliveryUpdate" method="get">
										<input type="hidden" name="containerNo" value="${bizDeliveryList.containerNo}">
										<button type="submit" class="btn btn-dark btn-sm">배송</button>
									</form>
								</c:when>
								<%-- 배송 완료 처리 : 배송중일 때만 가능 --%>
								<c:when test="${bizDeliveryList.contractDeliveryStatus eq '배송중'}">
									<form action="/admin/bizDeliveryComplete" method="post" onsubmit="return confirm('배송 완료 처리하시겠습니까?');">
										<input type="hidden" name="contractDeliveryNo" value="${bizDeliveryList.contractDeliveryNo}">
										<button type="submit" class="btn btn-dark btn-sm">완료</button>
									</form>
								</c:when>
								<c:otherwise>
									<button type="button" class="btn btn-muted btn-sm" disabled>배송</button>
									<button type="button" class="btn btn-muted btn-sm" disabled>완료</button>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	
	</div>
	
<!-- 공통 풋터 -->
<%@include file="/WEB-INF/common/footer/footer.jsp"%>

<!-- JS -->
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.datatables.net/1.13.8/js/jquery.dataTables.min.js"></script>
<script src="https://cdn.datatables.net/1.13.8/js/dataTables.bootstrap5.min.js"></script>
<script>
	$(function() {
		$('#table').DataTable({
			scrollX: true,  // 가로 스크롤 활성화
			searching : true, // 검색창 표시 여부
			ordering : true, // 정렬 기능(컬럼 헤더 클릭 시 오름/내림차순 정렬)
			paging : true, // 페이징
			pageLength : 10, // 페이지당 행 수
			lengthMenu : [10, 25, 50], // 페이징당 행 수 선택 옵션
			info : true, // "총 x건 중 n–m" 정보 표시
			autoWidth : false, // 자동 열 너비 계산 비활성화 (CSS width 우선 적용)

			// 한글 옵션
			language : {
				search : '<i class="bi bi-search"></i>', // 검색창 왼쪽 레이블
				lengthMenu : '_MENU_ 개씩 보기', // 페이지당 행 개수 드롭다운 문구
				info : '총 _TOTAL_건 중 _START_–_END_', // 페이지 정보 문구
				infoEmpty : '조회된 데이터가 없습니다.', // 데이터가 없을 때 info 영역 문구
				zeroRecords : '일치하는 데이터가 없습니다.', // 검색 결과가 없을 때 문구
				paginate : { // 페이지네이션 버튼 텍스트
					first : '처음', last : '마지막', next : '다음', previous : '이전'
				}
			}
		});
	});
</script>

</body>
</html>