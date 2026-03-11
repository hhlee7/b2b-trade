import http from 'k6/http';

/*
  k6 부하 테스트 설정

  - shared-iterations executor
	: 여러 VU(가상 사용자)가 전체 iteration을 나눠서 실행하는 방식

  - vus: 20
	: 동시에 요청을 보내는 가상 사용자 수

  - iterations: 20
	: 전체 테스트 동안 실행할 총 요청 횟수
	  (즉, 20명의 사용자가 동시에 1번씩 요청하는 상황 재현)

  - maxDuration: '10s'
	: 테스트가 예상보다 오래 걸릴 경우 강제 종료되는 최대 시간
*/

export const options = {
	scenarios: {
		delivery_once: {
			executor: 'shared-iterations',
			vus: 20,              // 동시 사용자 수
			iterations: 20,       // 총 요청 횟수
			maxDuration: '10s',   // 최대 테스트 실행 시간
		},
	},
};

/*
  SESSION_ID는 k6 실행 시 환경 변수로 전달
  ex) k6 run -e SESSION_ID=xxxx delivery_test.js
*/
const sessionId = __ENV.SESSION_ID || '';

/*
  테스트 실행 함수
  - 각 VU가 실행할 실제 HTTP 요청 정의
*/

export default function() {
	// 배송 처리 API 엔드포인트
	const url = 'http://localhost/admin/bizDeliveryUpdate';
	
	/*
	  POST 요청 payload
	  - application/x-www-form-urlencoded 방식
	  - 실제 브라우저 Network 탭에서 확인한 요청 파라미터 기준
	*/
	const payload = 'containerNo=8&deliveryCompany=TEST_DELIVERY_COMPANY&trackingNo=000000000000';

	/*
	  요청 옵션

	  redirects: 0
	    - 서버에서 302 redirect가 발생해도 자동으로 따라가지 않도록 설정
	    - 실제 응답을 확인하기 위해 사용

	  headers
	    Content-Type
	      - form-urlencoded 요청임을 명시

	    Cookie
	      - Spring Security 세션 인증을 위해 브라우저에서 발급된 JSESSIONID 사용
	      - 로그인 상태가 아니면 /admin 경로 접근이 차단되므로 필요
	*/
	const params = {
		redirects: 0,
		headers: {
			'Content-Type': 'application/x-www-form-urlencoded',

			/*
			  Spring Security 세션 인증
			  JSESSIONID를 환경 변수에서 주입
			*/
			'Cookie': `JSESSIONID=${sessionId}`
		}
	};
	
	// 배송 처리 POST 요청 실행
	http.post(url, payload, params);
}