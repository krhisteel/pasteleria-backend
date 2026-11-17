CREATE TABLE detalle_boleta (
    id BIGINT NOT NULL AUTO_INCREMENT,
    boleta_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    imagen VARCHAR(500) NOT NULL,
    cantidad INT NOT NULL,
    precio_base DECIMAL(16,2) NOT NULL,
    descuento_aplicado DECIMAL(5,2) NOT NULL,
    precio_final DECIMAL(16,2) NOT NULL,
    subtotal DECIMAL(16,2) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_detalle_boleta_producto UNIQUE (boleta_id, producto_id),
    INDEX ix_detalle_producto (producto_id),
    CONSTRAINT fk_detalle_boleta FOREIGN KEY (boleta_id)
        REFERENCES boleta(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (producto_id)
        REFERENCES producto(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT ck_detalle_cantidad CHECK (cantidad BETWEEN 1 AND 50),
    CONSTRAINT ck_detalle_descuento CHECK (descuento_aplicado BETWEEN 0 AND 100),
    CONSTRAINT ck_detalle_importes CHECK (precio_base > 0 AND precio_final >= 0 AND subtotal >= 0 AND subtotal = precio_final * cantidad),
    CONSTRAINT ck_detalle_redondeo CHECK (precio_final = ROUND(precio_base * (1 - descuento_aplicado / 100), 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
