create table orders (id serial primary key, order_date varchar(40), totalUSD NUMERIC(20,10));
create table items (id serial primary key, order_id serial not null, model VARCHAR(20), type VARCHAR(20), price NUMERIC(20,10), currency VARCHAR(3), priceUSD NUMERIC(20,10), FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE);
create table buyers (id serial primary key, name VARCHAR(40), address VARCHAR(100), email VARCHAR(40));
