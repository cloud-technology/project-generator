CREATE TABLE goods (
	id varchar(60) NOT NULL,
	goods_name varchar(50) NOT NULL,
	unit_price integer NOT NULL,
	inventory integer NOT NULL,
	current_versions integer NOT NULL,
	created_by varchar(60) NOT NULL,
	created_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
	last_modified_by varchar(60) NOT NULL,
	last_modified_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE goods ADD CONSTRAINT goods_pk PRIMARY KEY(id);
ALTER TABLE goods ADD CONSTRAINT goods_un UNIQUE (goods_name);

COMMENT ON COLUMN goods.id IS '商品編號';
COMMENT ON COLUMN goods.goods_name IS '商品名稱';
COMMENT ON COLUMN goods.unit_price IS '單價';
COMMENT ON COLUMN goods.inventory IS '庫存數量';
COMMENT ON COLUMN goods.current_versions IS '版本鎖';
COMMENT ON COLUMN goods.created_by IS '建立者';
COMMENT ON COLUMN goods.created_date IS '建立時間';
COMMENT ON COLUMN goods.last_modified_by IS '最後異動者';
COMMENT ON COLUMN goods.last_modified_date IS '最後異動時間';
