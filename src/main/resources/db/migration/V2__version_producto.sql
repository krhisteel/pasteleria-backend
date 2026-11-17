-- Preparacion fisica. Producto.java y el contrato PUT no se modifican en esta etapa.
-- El control optimista SOLO sera efectivo al incorporar @Version y validar la version recibida.
ALTER TABLE producto ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
