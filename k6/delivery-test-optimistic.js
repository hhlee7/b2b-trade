import http from 'k6/http';
import { check } from 'k6';

/*
  k6 동시 요청 테스트 설정

  - shared-iterations executor
	: 여러 VU(가상 사용자)가 전체 iteration을 나눠 실행

  - vus: 20
	: 동시에 요청을 보내는 가상 사용자 수

  - iterations: 20
	: 총 20번의 요청 수행
	  (즉, 동일 배송 완료 요청이 거의 동시에 여러 번 들어오는 상황 재현)

  - maxDuration: '10s'
	: 테스트 최대 실행 시간
*/
export const options = {
	scenarios: {
		delivery_complete_once: {
			executor: 'shared-iterations',
			vus: 20,
			iterations: 20,
			maxDuration: '10s',
		},
	},
};

/*
  SESSION_ID는 k6 실행 시 환경 변수로 전달
  ex)
  k6 run -e SESSION_ID=xxxx optimistic_delivery_complete_test.js
*/
const sessionId = __ENV.SESSION_ID || '';

export default function() {
	/*
	  배송 완료 처리 API 엔드포인트
	  - 실제 컨트롤러 매핑 기준
	*/
	const url = 'http://localhost/admin/bizDeliveryComplete';

	/*
	  POST 요청 payload
	  - 브라우저 Network 탭 또는 Talend 테스트 기준으로 실제 전송 파라미터 확인 필요
	  - 현재 서비스 로직상 핵심 식별자는 contractDeliveryNo
	*/
	const payload = 'contractDeliveryNo=37';

	/*
	  요청 옵션
	  - redirects: 0
		: 302 redirect 자동 이동 방지
		: 컨트롤러가 redirect:/admin/bizDeliveryList 반환하므로 응답 확인용
  
	  - Cookie
		: Spring Security 세션 인증용 JSESSIONID
	*/
	const params = {
		redirects: 0,
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded',
			'Cookie': `JSESSIONID=${sessionId}`,
		},
	};

	// 배송 완료 처리 POST 요청 실행
	const res = http.post(url, payload, params);

	/*
	  응답 검증
	  - 정상 처리/중복 처리 모두 redirect(302)일 수 있으므로
		최소한 응답이 정상 반환되는지만 확인
	*/
	check(res, {
		'status is 302 or 200': (r) => r.status === 302 || r.status === 200,
	});
}