# Backend Context

Backend la Spring Boot trong thu muc `backend`, Java 17, Maven, MySQL, JWT, MQTT.

## Chay backend

```bash
cd backend
./mvnw spring-boot:run
```

Neu khong co `mvnw`, dung Maven local:

```bash
cd backend
mvn spring-boot:run
```

Config chinh nam o `backend/src/main/resources/application.yml`.

## Package quan trong

- `controller`: REST API cho frontend.
- `service`: business logic, mapping command MQTT, lich su, sensor.
- `dto`: response/request contract voi frontend.
- `entity`: JPA entity.
- `repository`: Spring Data JPA repository.
- `specification`: filter/search cho bang history.
- `security`: JWT filter, token provider, user details.
- `config`: Security/CORS, WebSocket, MQTT.

## Contract voi frontend

Tat ca API nen tra `ApiResponse<T>`:

```json
{
  "status": "success",
  "message": "...",
  "data": {}
}
```

Frontend doc `payload.data ?? payload`, nen co the debug truc tiep payload nhung nen giu wrapper tren.

## Endpoints dang can giu on dinh

- `POST /api/auth/login`
- `GET /api/users/profile`
- `GET /api/devices`
- `POST /api/devices/control`
- `GET /api/sensors/latest`
- `GET /api/sensors/chart-data`
- `GET /api/sensors/history`
- `GET /api/actions/history`

## Luu y de khop frontend

- Field thoi gian tra ra DTO la `timestamp`, khong phai `time` hay `createdAt`.
- Query sort frontend gui `sortBy=timestamp`; backend map ve field entity bang `PageSortFactory`.
- Device dashboard can `DeviceDto.status` va `DeviceControlResponse.status` la `ON/OFF`.
- Action history co the luu `Loading` trong database de cho MQTT ack, nhung response control tra ve frontend nen la action vua yeu cau de toggle doi trang thai ngay.
- CORS trong `application.yml` phai cho phep `localhost/127.0.0.1` port `3000`, `5173`, `5500` va origin `null` neu mo HTML bang `file://`.

## MQTT flow

- Sensor payload vao `mqtt.topics.sensor-data`, service luu 3 dong sensor cung timestamp.
- Device control publish command vao `mqtt.topics.device-control`.
- Device response vao `mqtt.topics.device-response`, backend cap nhat ban ghi `ActionHistory` dang `Loading`.
- WebSocket topic dang dung:
  - `/topic/sensors/latest`
  - `/topic/device-response`
