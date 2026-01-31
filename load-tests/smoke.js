import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 5,
  duration: '30s',
  thresholds: {
    http_req_failed: ['rate<0.01'],   // <1% errors
    http_req_duration: ['p(95)<500'], // 95% under 500ms p95 < 500ms
  },
};

export default function () {
  const res = http.get('http://localhost:8080/feed/user-b?limit=5');
  check(res, {
    'status is 200': r => r.status === 200,
  });
  sleep(1);
}
