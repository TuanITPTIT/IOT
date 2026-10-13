# IoT Sensor System - Postman API

## Thong tin chung

- Base URL: `http://localhost:8080`
- Authentication: JWT Bearer Token
- Response wrapper:

```json
{
  "status": "success",
  "message": "Message",
  "data": {}
}
```

## Cach dung Postman

1. Import file `docs/IOT_Backend_Postman_Collection.json` vao Postman.
2. Chay request `Auth / Login`.
3. Token se tu dong duoc luu vao collection variable `jwtToken`.
4. Chay cac request con lai.

Tai khoan mac dinh:

```text
username: admin
password: password123
```

## 1. Dang nhap

### `POST /api/auth/login`

Dang nhap he thong va nhan JWT token.

Request body:

```json
{
  "username": "admin",
  "password": "password123"
}
```

Response data:

```json
{
    "status": "success",
    "message": "Login successful",
    "data": {
        "token": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc5MTkxMzI5MiwiZXhwIjoxNzkxOTk5NjkyfQ.qsH-oUZnqcuvVIGGBVQE9RMwAOiA6ScwKl9RQ5GoaoUOXy4CqU6jGw86iw6nMvi_LY9giN0cNm5wfVVDzqqwiw",
        "tokenType": "Bearer",
        "user": {
            "id": 1,
            "username": "admin",
            "fullName": "Lê Quốc Tuấn",
            "role": "ROLE_ADMIN"
        }
    }
}
```

## 2. Ho so nguoi dung

### `GET /api/users/profile`

Lay thong tin nguoi dung dang dang nhap.

Header:

```text
Authorization: Bearer {{jwtToken}}
```

Response data:

```json
{
  "id": 1,
  "username": "admin",
  "fullName": "Le Quoc Tuan",
  "role": "ROLE_ADMIN",
  "studentId": "B23DCCN885",
  "className": "D23CNPM02",
  "institution": "PTIT",
  "aboutMe": "...",
  "contact": {
    "phone": "...",
    "email": "...",
    "address": "..."
  },
  "resources": {
    "githubLink": "...",
    "figmaLink": "...",
    "apiDocsLink": "...",
    "baocaoLink": "..."
  }
}
```

## 3. Du lieu cam bien

### `GET /api/sensors/latest`

Lay bo du lieu cam bien moi nhat.

Response data:

```json
{
  "temperature": 31.8,
  "luminosity": 321,
  "humidity": 54,
  "timestamp": "2026-10-13T20:43:10"
}
```

### `GET /api/sensors/chart-data?limit=20`

Lay du lieu ve bieu do dashboard.

Query params:

| Ten | Kieu | Mo ta |
| --- | --- | --- |
| `limit` | number | So diem du lieu moi nhat, mac dinh `20` |

Response data:

```json
[
  {
    "time": "20:43:10",
    "temperature": 31.8,
    "humidity": 54,
    "luminosity": 321
  }
]
```

### `GET /api/sensors/history`

Lay lich su du lieu cam bien co phan trang va bo loc.

Query params:

| Ten | Kieu | Mo ta |
| --- | --- | --- |
| `page` | number | Trang hien tai, bat dau tu `0` |
| `size` | number | So ban ghi moi trang |
| `sortBy` | string | Truong sap xep, dung `timestamp` |
| `sortDir` | string | `asc` hoac `desc` |
| `keyword` / `query` | string | Tu khoa tim kiem |
| `sensorType` | string | `Temperature`, `Humidity`, `Luminosity` |
| `value` | string | Gia tri cam bien can tim |
| `fromTime` | string | Thoi gian bat dau |
| `toTime` | string | Thoi gian ket thuc |

Vi du:

```text
GET /api/sensors/history?page=0&size=5&sortBy=timestamp&sortDir=desc&sensorType=Temperature
```

Response data:

```json
{
  "content": [
    {
      "id": 1,
      "sensorId": 1,
      "sensorType": "Temperature",
      "deviceId": 1,
      "deviceName": "Temperature",
      "value": 31.8,
      "unit": "C",
      "timestamp": "2026-10-13T20:43:10"
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1
}
```

## 4. Thiet bi

### `GET /api/devices`

Lay danh sach thiet bi dieu khien.

Response data:

```json
[
  {
    "id": 1,
    "code": "RED",
    "name": "Temperature",
    "status": "OFF"
  },
  {
    "id": 2,
    "code": "YELLOW",
    "name": "Luminosity",
    "status": "OFF"
  },
  {
    "id": 3,
    "code": "GREEN",
    "name": "Humidity",
    "status": "OFF"
  }
]
```

### `POST /api/devices/control`

Gui lenh dieu khien thiet bi qua MQTT.

Request body bat/tat mot thiet bi:

```json
{
  "deviceId": 1,
  "action": "ON"
}
```

Request body bat/tat tat ca thiet bi:

```json
{
  "deviceCode": "ALL",
  "action": "OFF"
}
```

Gia tri:

| Gia tri | Mo ta |
| --- | --- |
| `deviceId=1` | LED Temperature, MQTT code `RED` |
| `deviceId=2` | LED Luminosity, MQTT code `YELLOW` |
| `deviceId=3` | LED Humidity, MQTT code `GREEN` |
| `action` | `ON` hoac `OFF` |

Response data:

```json
{
  "actionId": 10,
  "deviceId": 1,
  "deviceCode": "RED",
  "deviceName": "Temperature",
  "action": "ON",
  "status": "ON",
  "timestamp": "2026-10-13T20:43:10"
}
```

## 5. Lich su thao tac

### `GET /api/actions/history`

Lay lich su bat/tat thiet bi co phan trang va bo loc.

Query params:

| Ten | Kieu | Mo ta |
| --- | --- | --- |
| `page` | number | Trang hien tai, bat dau tu `0` |
| `size` | number | So ban ghi moi trang |
| `sortBy` | string | Truong sap xep, dung `timestamp` |
| `sortDir` | string | `asc` hoac `desc` |
| `deviceId` | number | ID thiet bi |
| `deviceName` | string | `Temperature`, `Luminosity`, `Humidity` |
| `action` | string | `ON` hoac `OFF` |
| `status` | string | `ON`, `OFF`, `Loading`, `Failed` |
| `time` | string | Tim theo ngay/gio |
| `fromTime` | string | Thoi gian bat dau |
| `toTime` | string | Thoi gian ket thuc |

Format `time` ho tro:

```text
13/10/2026
13-10-2026
2026-10-13
20:43:10 13/10/2026
13/10/2026 20:43:10
2026-10-13 20:43:10
```

Vi du:

```text
GET /api/actions/history?page=0&size=5&sortBy=timestamp&sortDir=desc&time=20:43:10%2013/10/2026
```

Response data:

```json
{
  "content": [
    {
      "id": 10,
      "deviceId": 1,
      "deviceName": "Temperature",
      "sensorId": 1,
      "sensorName": "Temperature",
      "action": "ON",
      "status": "ON",
      "timestamp": "2026-10-13T20:43:10"
    }
  ],
  "page": 0,
  "size": 5,
  "totalElements": 1,
  "totalPages": 1
}
```

## MQTT lien quan

ESP8266 publish du lieu sensor vao topic:

```text
sensor_data
```

Payload:

```json
{
  "temperature": 31.8,
  "humidity": 54,
  "light": 321
}
```

Backend publish lenh dieu khien vao topic:

```text
device_control
```

Payload:

```json
{
  "deviceCode": "RED",
  "command": "ON"
}
```

ESP8266 publish ket qua dieu khien vao topic:

```text
device_response
```

Payload:

```json
{
  "deviceCode": "RED",
  "command": "ON",
  "status": "SUCCESS"
}
```
