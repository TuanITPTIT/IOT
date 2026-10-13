INSERT INTO users (id, username, password, full_name, role, created_at, github_link, figma_link, apidocs_link, baocao_link)
VALUES (1, 'admin', '$2a$10$MyXJygPoB071RZz45OrYLu6hFRiWRvNewnyt8fQrcpOo5ohc6ddZi', 'Lê Quốc Tuấn', 'ROLE_ADMIN', CURRENT_TIMESTAMP,
        'https://github.com/tuanthanhmc123/iot-smart-home',
        'https://figma.com/@tuanthanhmc/smart-home-ui',
        -- 'https://documenter.getpostman.com/view/iot-apis',
        'https://tuanthanhmc123-1193583.postman.co/workspace/69c0412c-0f5e-4b4e-8a36-bfb9345cb800/collection/58929364-8fb2b22d-0208-4dff-abb4-da909e314de3',
        'https://github.com/tuanthanhmc123/iot-smart-home/blob/main/docs/Bao_Cao_Do_An_IoT_LeQuocTuan.pdf')
ON DUPLICATE KEY UPDATE
    username = VALUES(username),
    password = VALUES(password),
    full_name = VALUES(full_name),
    role = VALUES(role),
    github_link = VALUES(github_link),
    figma_link = VALUES(figma_link),
    apidocs_link = VALUES(apidocs_link),
    baocao_link = VALUES(baocao_link);

INSERT INTO sensors (id, name, created_at) VALUES
    (1, 'Temperature', CURRENT_TIMESTAMP),
    (2, 'Luminosity', CURRENT_TIMESTAMP),
    (3, 'Humidity', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO devices (id, name, created_at) VALUES
    (1, 'Temperature', CURRENT_TIMESTAMP),
    (2, 'Luminosity', CURRENT_TIMESTAMP),
    (3, 'Humidity', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name = VALUES(name);
