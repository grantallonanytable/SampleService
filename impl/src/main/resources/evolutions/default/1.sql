# --- !Ups

CREATE TABLE "read_side_offsets" (
  "read_side_id" VARCHAR(255),
  "tag" VARCHAR(255),
  "sequence_offset" NUMBER,
  "time_uuid_offset" CHAR(36),
  PRIMARY KEY ("read_side_id", "tag")
);

CREATE TABLE ORDERS (
  ID VARCHAR(256 CHAR) PRIMARY KEY,
  CREATION_DATE DATE NOT NULL,
  CREATION_DATE_OFFSET NUMBER,
  ORDER_STATE VARCHAR(100 CHAR) NOT NULL,
  ORDER_TYPE VARCHAR(100 CHAR) NOT NULL,
  REGION VARCHAR(100 CHAR),
  CITY VARCHAR(100 CHAR),
  SALEPOINT_ID VARCHAR(200 CHAR),
  SALEPOINT_ADDRESS VARCHAR(200 CHAR),
  SHIPGROUP_TYPE NUMBER ,
  CLIENT_NAME VARCHAR(200 CHAR),
  TOTAL_PRICE NUMBER,
  PAY_TYPE VARCHAR(200 CHAR),
  PAY_STATE VARCHAR(200 CHAR),
  TRACK_NUMBER VARCHAR(200 CHAR),
  COMMENTARY VARCHAR(500 CHAR),
  ADDRESS_DELIVERY VARCHAR(200 CHAR),
  CLIENT_EMAIL VARCHAR(200 CHAR),
  CLIENT_PHONE VARCHAR(12 CHAR),
  PROMO VARCHAR(200 CHAR),
  PARENT_NETWORK VARCHAR(200 CHAR),
  NETWORK VARCHAR(200 CHAR),
  PARTNER VARCHAR(200 CHAR),
  CANCEL_REASON VARCHAR(200 CHAR),
  MNP VARCHAR(12 CHAR)
);

CREATE TABLE ORDER_ITEM (
  ORDER_ID VARCHAR(256 CHAR) NOT NULL,
  ITEM_STATE VARCHAR(100 CHAR),
  ITEM_COMMERCE_ID VARCHAR(256 CHAR),
  ITEM_CATALOG_PRODUCT_ID VARCHAR(256 CHAR),
  ITEM_CATALOG_SKU_ID VARCHAR(256 CHAR),
  ITEM_CATALOG_SERIAL_ID VARCHAR(256 CHAR),
  ITEM_TYPE VARCHAR(100 CHAR),
  ITEM_FRONT_NAME VARCHAR(200 CHAR),
  ITEM_MNP VARCHAR(12 CHAR),
  ITEM_AMOUNT NUMBER,
  ITEM_PRICE_AMOUNT NUMBER,
  ITEM_PRICE_CURRENCY VARCHAR(3 CHAR),
  ITEM_SALEPRICE_AMOUNT NUMBER,
  ITEM_SALEPRICE_CURRENCY VARCHAR(3 CHAR),
  SUBITEM_COMMERCE_ID VARCHAR(256 CHAR),
  SUBITEM_CATALOG_PRODUCT_ID VARCHAR(256 CHAR),
  SUBITEM_CATALOG_SKU_ID VARCHAR(256 CHAR),
  SUBITEM_CATALOG_SERIAL_ID VARCHAR(256 CHAR),
  SUBITEM_TYPE VARCHAR(100 CHAR),
  SUBITEM_FRONT_NAME VARCHAR(200 CHAR),
  SUBITEM_AMOUNT NUMBER,
  SUBITEM_PRICE_AMOUNT NUMBER,
  SUBITEM_PRICE_CURRENCY VARCHAR(3 CHAR),
  SUBITEM_SALEPRICE_AMOUNT NUMBER,
  SUBITEM_SALEPRICE_CURRENCY VARCHAR(3 CHAR),
  CONSTRAINT ORDER_ITEM_FK FOREIGN KEY (ORDER_ID) REFERENCES ORDERS(ID)
);

# --- !Downs

DROP TABLE ORDER_ITEM;
DROP TABLE ORDERS;
DROP TABLE "read_side_offsets";