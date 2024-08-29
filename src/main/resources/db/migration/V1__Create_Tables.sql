CREATE TABLE IF NOT EXISTS customer (
        id          BIGSERIAL PRIMARY KEY,
        reference_id VARCHAR(128) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS payment (
       id            BIGSERIAL PRIMARY KEY,
       amount        NUMERIC(15, 2) NOT NULL,
       currency      VARCHAR(64) NOT NULL,
       customer_id   BIGINT NOT NULL,
       created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       payment_type   VARCHAR(64) NOT NULL,
       payment_method VARCHAR(64) NOT NULL,
       FOREIGN KEY (customer_id) REFERENCES customer(id)
);

CREATE TABLE IF NOT EXISTS payment_request (
       id              BIGSERIAL PRIMARY KEY,
       payment_id      BIGINT NOT NULL,
       request_body    TEXT NOT NULL,
       created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
       FOREIGN KEY (payment_id) REFERENCES payment(id)
);

CREATE TABLE IF NOT EXISTS payment_response (
        id              BIGSERIAL PRIMARY KEY,
        request_id      BIGINT NOT NULL,
        response_id     VARCHAR(128),
        response_body   TEXT NOT NULL,
        status_code     INT NOT NULL,
        created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (request_id) REFERENCES payment_request(id)
);
