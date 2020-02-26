# --- !Ups
CREATE TABLE ORDER_DELIVERY (
    ID NUMBER PRIMARY KEY,
    DELIVERY_ID VARCHAR(256 CHAR),
    DELIVERY_NAME VARCHAR(100 CHAR),
    DELIVERY_ALIAS VARCHAR(100 CHAR),
    CITY_ID VARCHAR(256 CHAR),
    TRACK_NUMBER VARCHAR(200 CHAR),
    SHIPGROUP_TYPE NUMBER,
    COURIER_INFO_POSTALCODE VARCHAR(10 CHAR),
    COURIER_INFO_CITY VARCHAR(100 CHAR),
    COURIER_INFO_STREET VARCHAR(256 CHAR),
    COURIER_INFO_BUILDING VARCHAR(10 CHAR),
    COURIER_INFO_APARTMENT VARCHAR(10 CHAR),
    SALE_POINT_ID VARCHAR(200 CHAR),
    SALE_POINT_ADDRESS VARCHAR(200 CHAR),
    TARIFF_ZONE_ID VARCHAR(256 CHAR),
    TARIFF_ZONE_NAME VARCHAR(100 CHAR),
    TARIFF_ZONE_DESC VARCHAR(256 CHAR),
    TARIFF_ZONE_COST_AMOUNT NUMBER,
    TARIFF_ZONE_COST_CURRENCY VARCHAR(3 CHAR)
);

CREATE TABLE ORDER_CLIENT (
  ID NUMBER PRIMARY KEY,
  NAME VARCHAR(200 CHAR),
  GENDER VARCHAR(10 CHAR),
  PHONE VARCHAR(12 CHAR),
  EMAIL VARCHAR(200 CHAR),
  IDENT_TYPE VARCHAR(100 CHAR),
  IDENT_SERIES VARCHAR(50 CHAR),
  IDENT_NUMBER VARCHAR(50 CHAR),
  IDENT_ISSUEDBY VARCHAR(256 CHAR),
  IDENT_ISSUEDON DATE,
  IDENT_ISSUEDCODE VARCHAR(10 CHAR),
  IDENT_BIRTHDAY DATE,
  IDENT_PLACEOFBIRTH VARCHAR(256 CHAR),
  ADDRESS_POSTALCODE VARCHAR(10 CHAR),
  ADDRESS_CITY VARCHAR(100 CHAR),
  ADDRESS_STREET VARCHAR(256 CHAR),
  ADDRESS_BUILDING VARCHAR(10 CHAR),
  ADDRESS_APARTMENT VARCHAR(10 CHAR)
);

ALTER TABLE ORDERS RENAME COLUMN REGION TO SITE_ID;

ALTER TABLE ORDERS RENAME COLUMN ORDER_STATE TO STATE;

ALTER TABLE ORDERS DROP (
  CITY,
  TRACK_NUMBER,
  SHIPGROUP_TYPE,
  CLIENT_NAME,
  CLIENT_EMAIL,
  CLIENT_PHONE,
  SALEPOINT_ID,
  SALEPOINT_ADDRESS,
  ADDRESS_DELIVERY,
  TOTAL_PRICE
);

ALTER TABLE ORDERS ADD (
  DELIVERY_ID NUMBER,
  CLIENT_ID NUMBER,
  TOTAL_PRICE_AMOUNT NUMBER,
  TOTAL_PRICE_CURRENCY VARCHAR(3 CHAR)
);

ALTER TABLE ORDERS ADD CONSTRAINT ORDER_DELIVERY_FK FOREIGN KEY (DELIVERY_ID) REFERENCES ORDER_DELIVERY(ID);

ALTER TABLE ORDERS ADD CONSTRAINT ORDER_CLIENT_FK FOREIGN KEY (CLIENT_ID) REFERENCES ORDER_CLIENT(ID);

CREATE SEQUENCE DELIVERY_SEQ START WITH 1 MINVALUE 1 MAXVALUE 999999999999999999;

CREATE SEQUENCE CLIENT_SEQ START WITH 1 MINVALUE 1 MAXVALUE 999999999999999999;

# --- !Downs

ALTER TABLE ORDERS DROP (
  DELIVERY_ID,
  CLIENT_ID,
  TOTAL_PRICE_AMOUNT,
  TOTAL_PRICE_CURRENCY
);

ALTER TABLE ORDERS ADD (
  CITY VARCHAR(100 CHAR),
  TRACK_NUMBER VARCHAR(200 CHAR),
  SHIPGROUP_TYPE NUMBER,
  CLIENT_NAME VARCHAR(200 CHAR),
  CLIENT_EMAIL VARCHAR(200 CHAR),
  CLIENT_PHONE VARCHAR(12 CHAR),
  SALEPOINT_ID VARCHAR(200 CHAR),
  SALEPOINT_ADDRESS VARCHAR(200 CHAR),
  ADDRESS_DELIVERY VARCHAR(200 CHAR),
  TOTAL_PRICE NUMBER
);

ALTER TABLE ORDERS RENAME COLUMN SITE_ID TO REGION;

ALTER TABLE ORDERS RENAME COLUMN STATE TO ORDER_STATE;

DROP TABLE ORDER_DELIVERY;

DROP TABLE ORDER_CLIENT;

DROP SEQUENCE CLIENT_SEQ;

DROP SEQUENCE DELIVERY_SEQ;
