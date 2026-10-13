# Frontend Context

Frontend la static HTML/CSS/JS trong thu muc `frontend`.

## Chay giao dien

- Mo truc tiep cac file trong `frontend/pages`, hoac dung Live Server tren port `5500`.
- Backend mac dinh: `http://localhost:8080`, cau hinh tai `frontend/js/config.js`.
- Dang nhap demo: `admin` / `password123`.
- `USE_MOCK_WHEN_API_FAILS=false` de test voi backend/ESP that, khong hien du lieu mock khi API loi.

## File chinh

- `frontend/pages/index.html` + `frontend/js/auth.js`: man hinh dang nhap, goi `POST /api/auth/login`.
- `frontend/pages/dashboard.html` + `frontend/js/dashboard.js`: dashboard cam bien, bieu do, dieu khien thiet bi.
- `frontend/pages/data.html` + `frontend/js/data.js`: bang lich su du lieu cam bien.
- `frontend/pages/history.html` + `frontend/js/history.js`: bang lich su thao tac thiet bi.
- `frontend/pages/profile.html` + `frontend/js/profile.js`: thong tin ca nhan.
- `frontend/js/common.js`: helper chung, auth token, fetch wrapper, normalize response, mock data.

## API frontend dang dung

Moi response backend nen co dang:

```json
{
  "status": "success",
  "message": "...",
  "data": {}
}
```

Endpoints:

- `POST /api/auth/login`
  - Body: `{ "username": "...", "password": "..." }`
  - Data: `{ "token": "...", "tokenType": "Bearer", "user": { "id", "username", "fullName", "role" } }`
- `GET /api/users/profile`
  - Data gom `fullName`, `role`, `studentId`, `className`, `institution`, `aboutMe`, `contact`, `resources`.
- `GET /api/devices`
  - Data la list: `{ "id", "code", "name", "status" }`.
  - `status` nen la `ON` hoac `OFF` de dashboard render toggle dung.
- `POST /api/devices/control`
  - Body: `{ "deviceId": 1, "action": "ON" }`.
  - Data: `{ "actionId", "deviceId", "deviceCode", "deviceName", "action", "status", "timestamp" }`.
  - Dashboard dung `status` de cap nhat toggle, nen backend tra `ON/OFF` ngay sau khi gui len MQTT.
- `GET /api/sensors/latest`
  - Data: `{ "temperature", "humidity", "luminosity", "timestamp" }`.
- `GET /api/sensors/chart-data?limit=20`
  - Data la list: `{ "time", "temperature", "humidity", "luminosity" }`.
- `GET /api/sensors/history?page=0&size=1000&sortBy=timestamp&sortDir=desc`
  - Data la page: `{ "content", "page", "size", "totalElements", "totalPages" }`.
  - Moi item: `{ "id", "sensorType", "value", "unit", "timestamp" }`.
- `GET /api/actions/history?page=0&size=5&sortBy=timestamp&sortDir=desc`
  - Filter FE co the gui: `deviceName`, `action`, `status`, `time`.
  - Moi item: `{ "id", "deviceName", "action", "status", "timestamp" }`.
