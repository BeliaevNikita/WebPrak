-- AUTHORS
INSERT INTO authors (first_name, last_name) VALUES
                                                ('Лев', 'Толстой'),
                                                ('Фёдор', 'Достоевский'),
                                                ('Александр', 'Пушкин'),
                                                ('Антон', 'Чехов'),
                                                ('Николай', 'Гоголь'),
                                                ('Иван', 'Тургенев'),
                                                ('Михаил', 'Булгаков'),
                                                ('Максим', 'Горький'),
                                                ('Владимир', 'Набоков'),
                                                ('Борис', 'Пастернак');

-- WORKS
INSERT INTO works (title, description) VALUES
                                           ('Война и мир', 'Роман-эпопея'),
                                           ('Анна Каренина', 'Роман'),
                                           ('Преступление и наказание', 'Роман'),
                                           ('Идиот', 'Роман'),
                                           ('Евгений Онегин', 'Роман в стихах'),
                                           ('Капитанская дочка', 'Повесть'),
                                           ('Мёртвые души', 'Поэма'),
                                           ('Ревизор', 'Комедия'),
                                           ('Мастер и Маргарита', 'Роман'),
                                           ('Доктор Живаго', 'Роман');

-- PUBLICATIONS (связь)
INSERT INTO publications (work_id, author_id) VALUES
                                                  (1,1),(2,1),
                                                  (3,2),(4,2),
                                                  (5,3),(6,3),
                                                  (7,5),(8,5),
                                                  (9,7),
                                                  (10,10);

-- COVERS
INSERT INTO covers (cover_name) VALUES
                                    ('Твердый переплет'),
                                    ('Мягкая обложка'),
                                    ('Суперобложка');

-- EDITIONS
INSERT INTO editions (work_id, cover_id, page_count, publication_date, publisher, language, quantity, price) VALUES
                                                                                                                 (1,1,1200,'2010-01-01','Эксмо','RU',10,999.99),
                                                                                                                 (2,2,800,'2012-05-10','АСТ','RU',5,599.50),
                                                                                                                 (3,1,600,'2015-03-15','Эксмо','RU',7,550.00),
                                                                                                                 (4,2,650,'2018-07-20','АСТ','RU',4,620.00),
                                                                                                                 (5,3,300,'2005-06-01','Азбука','RU',12,300.00),
                                                                                                                 (6,2,250,'2008-09-12','Азбука','RU',9,280.00),
                                                                                                                 (7,1,400,'2011-11-11','Эксмо','RU',6,450.00),
                                                                                                                 (8,2,200,'2013-02-14','АСТ','RU',8,320.00),
                                                                                                                 (9,1,500,'2020-10-10','Эксмо','RU',3,700.00),
                                                                                                                 (10,3,550,'2019-12-01','АСТ','RU',2,750.00);

-- USERS
INSERT INTO users (role, contacts, birth_date, gender, first_name, last_name, login, password) VALUES
                                                                                                   ('customer','{"email":"user1@mail.com"}','1990-01-01',true,'Иван','Иванов','user1','pass'),
                                                                                                   ('customer','{"email":"user2@mail.com"}','1992-02-02',true,'Петр','Петров','user2','pass'),
                                                                                                   ('customer','{"email":"user3@mail.com"}','1995-03-03',false,'Анна','Сидорова','user3','pass'),
                                                                                                   ('employee','{"email":"emp1@mail.com"}','1985-04-04',true,'Олег','Смирнов','emp1','pass'),
                                                                                                   ('customer','{"email":"user4@mail.com"}','1998-05-05',false,'Мария','Кузнецова','user4','pass'),
                                                                                                   ('customer','{"email":"user5@mail.com"}','2000-06-06',true,'Алексей','Попов','user5','pass'),
                                                                                                   ('customer','{"email":"user6@mail.com"}','1993-07-07',true,'Дмитрий','Васильев','user6','pass'),
                                                                                                   ('customer','{"email":"user7@mail.com"}','1991-08-08',false,'Елена','Новикова','user7','pass');

-- ORDERS
INSERT INTO orders (client_id, order_date, status, delivery_address) VALUES
                                                                         (1,'2024-01-01','delivered','Москва'),
                                                                         (2,'2024-01-02','in_processing','СПб'),
                                                                         (3,'2024-01-03','draft','Казань'),
                                                                         (1,'2024-01-04','delivered','Москва'),
                                                                         (4,'2024-01-05','delivered','Новосибирск'),
                                                                         (5,'2024-01-06','refund_attempt','Екатеринбург');

-- ORDER ITEMS
INSERT INTO order_items (order_id, edition_id, quantity, status) VALUES
                                                                     (1,1,1,'delivered'),
                                                                     (1,2,2,'delivered'),
                                                                     (2,3,1,'delivered'),
                                                                     (2,4,1,'delivered'),
                                                                     (3,5,1,'delivered'),
                                                                     (4,6,2,'delivered'),
                                                                     (4,7,1,'delivered'),
                                                                     (5,8,1,'delivered'),
                                                                     (6,9,1,'returned'),
                                                                     (6,10,1,'returned');