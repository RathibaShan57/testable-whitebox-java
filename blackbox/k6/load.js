import http from 'k6/http';
import { check, sleep } from 'k6';

/** Performance URL (API Service) — K6 load / latency signals against SampleApiServer. */
export const options = {
  vus: 5,
  duration: '15s',
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE = __ENV.BASE_URL || 'http://127.0.0.1:8089';

export default function () {
  const health = http.get(`${BASE}/health`);
  check(health, { 'health 200': (r) => r.status === 200 });

  const orders = http.get(`${BASE}/api/orders`);
  check(orders, { 'orders 200': (r) => r.status === 200 });

  sleep(0.3);
}
