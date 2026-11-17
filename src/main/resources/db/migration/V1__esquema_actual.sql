-- Referencia estructural de la copia MariaDB inspeccionada. Sin datos ni AUTO_INCREMENT fijado.
-- Solo se ejecuta en una base VACIA. En una base existente requiere revision y baseline explicito.
CREATE TABLE categoria (
    id BIGINT NOT NULL AUTO_INCREMENT,
    descripcion VARCHAR(500) NULL,
    nombre VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    apellidos VARCHAR(100) NOT NULL,
    codigo_promocional VARCHAR(255) NULL,
    comuna VARCHAR(255) NULL,
    correo VARCHAR(100) NOT NULL,
    direccion VARCHAR(300) NULL,
    fecha_nacimiento DATE NULL,
    fecha_registro DATETIME(6) NULL,
    nombre VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    region VARCHAR(255) NULL,
    rol ENUM('ADMIN','CLIENTE','VENDEDOR') NOT NULL,
    run VARCHAR(12) NULL,
    telefono VARCHAR(15) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_correo UNIQUE (correo),
    CONSTRAINT uk_usuario_run UNIQUE (run)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE producto (
    id BIGINT NOT NULL AUTO_INCREMENT,
    activo BIT(1) NOT NULL,
    descripcion VARCHAR(1000) NULL,
    imagen_url VARCHAR(500) NULL,
    nombre VARCHAR(150) NOT NULL,
    precio DECIMAL(12,2) NOT NULL,
    stock INT NOT NULL,
    categoria_id BIGINT NOT NULL,
    codigo VARCHAR(20) NULL,
    descuento DECIMAL(5,2) NULL,
    stock_critico INT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_producto_codigo UNIQUE (codigo),
    INDEX ix_producto_categoria (categoria_id),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id)
        REFERENCES categoria(id) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
