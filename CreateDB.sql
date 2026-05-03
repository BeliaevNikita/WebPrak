-- ENUM типы
CREATE TYPE user_role AS ENUM ('customer', 'employee');

CREATE TYPE order_status AS ENUM (
    'draft',
    'in_processing',
    'delivered',
    'refund_attempt',
    'refund_accepted',
    'partial_refund'
    );

CREATE TYPE item_status AS ENUM ('delivered', 'returned');

-- Таблица обложек
CREATE TABLE covers (
    cover_id SERIAL PRIMARY KEY,
    cover_name VARCHAR(40) UNIQUE
);

-- Таблица произведений
CREATE TABLE works (
    work_id SERIAL PRIMARY KEY,
    title VARCHAR(100),
    description TEXT
);

-- Таблица авторов
CREATE TABLE authors (
    author_id SERIAL PRIMARY KEY,
    first_name VARCHAR(20),
    last_name VARCHAR(40)
);

-- Связь многие-ко-многим (произведения - авторы)
CREATE TABLE publications (
    work_id INT,
    author_id INT,
    PRIMARY KEY (work_id, author_id),
    FOREIGN KEY (work_id) REFERENCES works(work_id) ON DELETE NO ACTION ,
    FOREIGN KEY (author_id) REFERENCES authors(author_id) ON DELETE NO ACTION
);

-- Издания
CREATE TABLE editions (
    edition_id SERIAL PRIMARY KEY,
    work_id INT REFERENCES works(work_id),
    cover_id INT REFERENCES covers(cover_id),
    page_count INT,
    publication_date DATE,
    publisher VARCHAR(100),
    language VARCHAR(20),
    quantity INT CHECK (quantity >= 0),
    price NUMERIC(10,2)
);

-- Пользователи
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    role user_role,
    contacts JSON,
    birth_date DATE,
    gender BOOLEAN,
    first_name VARCHAR(20),
    last_name VARCHAR(40),
    login VARCHAR(20) UNIQUE NOT NULL,
    password VARCHAR(255)
);

-- Заказы
CREATE TABLE orders (
    order_id SERIAL PRIMARY KEY,
    client_id INT REFERENCES users(user_id),
    order_date DATE,
    status order_status,
    delivery_address VARCHAR(100)
);

-- Позиции заказа
CREATE TABLE order_items (
     item_id SERIAL PRIMARY KEY,
     order_id INT REFERENCES orders(order_id) ON DELETE NO ACTION,
    edition_id INT REFERENCES editions(edition_id),
    quantity INT CHECK (quantity >= 0),
    status item_status
);