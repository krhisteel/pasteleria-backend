CREATE TABLE solicitud_compra (
    id BIGINT NOT NULL AUTO_INCREMENT,
    usuario_id BIGINT NOT NULL,
    clave VARCHAR(36) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    huella VARCHAR(64) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    fecha DATETIME(6) NOT NULL,
    boleta_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_solicitud_usuario_clave UNIQUE (usuario_id, clave),
    CONSTRAINT uk_solicitud_boleta UNIQUE (boleta_id),
    CONSTRAINT fk_solicitud_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuario(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_solicitud_boleta FOREIGN KEY (boleta_id)
        REFERENCES boleta(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_solicitud_clave CHECK (clave REGEXP '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'),
    CONSTRAINT ck_solicitud_huella CHECK (huella REGEXP '^[0-9a-f]{64}$')
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
