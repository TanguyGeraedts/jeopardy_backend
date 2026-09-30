-- Target Owner UUID
-- Owner ID: 00000000-0000-0000-0000-000000000000

--------------------------------------------------------------------------------
-- 1. SMALL QUIZ: "Quick Mini Trivia" (2 Categories x 2 Questions)
--------------------------------------------------------------------------------
INSERT INTO quizzes (id, owner_id, name)
VALUES ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Quick Mini Trivia');

-- Categories
INSERT INTO categories (id, quiz_id, name, sort_order) VALUES
                                                           ('11000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Animals', 1),
                                                           ('11000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000001', 'Food', 2);

-- Questions (Animals)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('11100000-0000-0000-0000-000000000001', '11000000-0000-0000-0000-000000000001', 100, 'What is the fastest land animal?', 'Cheetah', 'TEXT', NULL, false),
                                                                                                                         ('11100000-0000-0000-0000-000000000002', '11000000-0000-0000-0000-000000000001', 200, 'Identify this animal in the picture.', 'Panda', 'IMAGE', 'https://example.com/media/panda.jpg', false);

-- Questions (Food) - Fixed duplicate UUID: changed 11100000-...-0002 to 11100000-...-0004
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('11100000-0000-0000-0000-000000000003', '11000000-0000-0000-0000-000000000002', 100, 'What fruit is known as the king of fruits and has a distinct odor?', 'Durian', 'TEXT', NULL, false),
                                                                                                                         ('11100000-0000-0000-0000-000000000004', '11000000-0000-0000-0000-000000000002', 200, 'Which country invented Pizza?', 'Italy', 'TEXT', NULL, false);


--------------------------------------------------------------------------------
-- 2. MEDIUM QUIZ: "Standard Pop Culture" (4 Categories x 4 Questions)
--------------------------------------------------------------------------------
INSERT INTO quizzes (id, owner_id, name)
VALUES ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Standard Pop Culture');

-- Categories
INSERT INTO categories (id, quiz_id, name, sort_order) VALUES
                                                           ('21000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Movies', 1),
                                                           ('21000000-0000-0000-0000-000000000002', '20000000-0000-0000-0000-000000000001', 'Music', 2),
                                                           ('21000000-0000-0000-0000-000000000003', '20000000-0000-0000-0000-000000000001', 'Video Games', 3),
                                                           ('21000000-0000-0000-0000-000000000004', '20000000-0000-0000-0000-000000000001', 'Comics', 4);

-- Questions (Movies)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('21100000-0000-0000-0000-000000000001', '21000000-0000-0000-0000-000000000001', 100, 'Who directed Jurassic Park?', 'Steven Spielberg', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000002', '21000000-0000-0000-0000-000000000001', 200, 'Which 1994 movie features the quote "Life is like a box of chocolates"?', 'Forrest Gump', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000003', '21000000-0000-0000-0000-000000000001', 300, 'What character is shown in this clip?', 'Darth Vader', 'VIDEO', 'https://example.com/media/vader.mp4', true),
                                                                                                                         ('21100000-0000-0000-0000-000000000004', '21000000-0000-0000-0000-000000000001', 400, 'What was the highest-grossing film of 2019?', 'Avengers: Endgame', 'TEXT', NULL, false);

-- Questions (Music)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('21100000-0000-0000-0000-000000000005', '21000000-0000-0000-0000-000000000002', 100, 'Who is known as the King of Pop?', 'Michael Jackson', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000006', '21000000-0000-0000-0000-000000000002', 200, 'Which band released the album "Abbey Road"?', 'The Beatles', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000007', '21000000-0000-0000-0000-000000000002', 300, 'Listen to this clip: who is singing?', 'Adele', 'VIDEO', 'https://example.com/media/adele_song.mp4', false),
                                                                                                                         ('21100000-0000-0000-0000-000000000008', '21000000-0000-0000-0000-000000000002', 400, 'What is the real name of rapper Eminem?', 'Marshall Mathers', 'TEXT', NULL, false);

-- Questions (Video Games)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('21100000-0000-0000-0000-000000000009', '21000000-0000-0000-0000-000000000003', 100, 'Who is Nintendo''s main mascot?', 'Mario', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000010', '21000000-0000-0000-0000-000000000003', 200, 'Which game features the island of Los Santos?', 'Grand Theft Auto V', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000011', '21000000-0000-0000-0000-000000000003', 300, 'Name the main character from The Legend of Zelda series.', 'Link', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000012', '21000000-0000-0000-0000-000000000003', 400, 'Name this classic console shown in the image.', 'Sega Genesis / Mega Drive', 'IMAGE', 'https://example.com/media/genesis.jpg', false);

-- Questions (Comics)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('21100000-0000-0000-0000-000000000013', '21000000-0000-0000-0000-000000000004', 100, 'What is Bruce Wayne''s superhero alter ego?', 'Batman', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000014', '21000000-0000-0000-0000-000000000004', 200, 'Which metal is bonded to Wolverine''s skeleton?', 'Adamantium', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000015', '21000000-0000-0000-0000-000000000004', 300, 'What fictional city does Superman protect?', 'Metropolis', 'TEXT', NULL, false),
                                                                                                                         ('21100000-0000-0000-0000-000000000016', '21000000-0000-0000-0000-000000000004', 400, 'Who is the alter ego of Spider-Man?', 'Peter Parker', 'TEXT', NULL, false);


--------------------------------------------------------------------------------
-- 3. LARGE QUIZ: "Ultimate Knowledge Challenge" (6 Categories x 5 Questions)
--------------------------------------------------------------------------------
INSERT INTO quizzes (id, owner_id, name)
VALUES ('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Ultimate Knowledge Challenge');

-- Categories
INSERT INTO categories (id, quiz_id, name, sort_order) VALUES
                                                           ('31000000-0000-0000-0000-000000000001', '30000000-0000-0000-0000-000000000001', 'World Geography', 1),
                                                           ('31000000-0000-0000-0000-000000000002', '30000000-0000-0000-0000-000000000001', 'Science & Nature', 2),
                                                           ('31000000-0000-0000-0000-000000000003', '30000000-0000-0000-0000-000000000001', 'World History', 3),
                                                           ('31000000-0000-0000-0000-000000000004', '30000000-0000-0000-0000-000000000001', 'Literature', 4),
                                                           ('31000000-0000-0000-0000-000000000005', '30000000-0000-0000-0000-000000000001', 'Technology', 5),
                                                           ('31000000-0000-0000-0000-000000000006', '30000000-0000-0000-0000-000000000001', 'Sports', 6);

-- Questions (World Geography)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000001', '31000000-0000-0000-0000-000000000001', 200, 'What is the capital of Australia?', 'Canberra', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000002', '31000000-0000-0000-0000-000000000001', 400, 'Which river is the longest in the world?', 'Nile (or Amazon, depending on definition)', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000003', '31000000-0000-0000-0000-000000000001', 600, 'Identify this famous landmark.', 'Machu Picchu', 'IMAGE', 'https://example.com/media/machu_picchu.jpg', false),
                                                                                                                         ('31100000-0000-0000-0000-000000000004', '31000000-0000-0000-0000-000000000001', 800, 'What is the smallest country in the world by land area?', 'Vatican City', 'TEXT', NULL, true),
                                                                                                                         ('31100000-0000-0000-0000-000000000005', '31000000-0000-0000-0000-000000000001', 1000, 'Which mountain range separates Europe and Asia?', 'Ural Mountains', 'TEXT', NULL, false);

-- Questions (Science & Nature)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000006', '31000000-0000-0000-0000-000000000002', 200, 'What chemical element has the symbol Au?', 'Gold', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000007', '31000000-0000-0000-0000-000000000002', 400, 'What planet is known as the Red Planet?', 'Mars', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000008', '31000000-0000-0000-0000-000000000002', 600, 'What is the speed of light in vacuum (approximate in km/s)?', '300,000 km/s', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000009', '31000000-0000-0000-0000-000000000002', 800, 'What process do plants use to convert sunlight into food?', 'Photosynthesis', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000010', '31000000-0000-0000-0000-000000000002', 1000, 'What is the hardest natural substance on Earth?', 'Diamond', 'TEXT', NULL, false);

-- Questions (World History)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000011', '31000000-0000-0000-0000-000000000003', 200, 'In which year did World War II end?', '1945', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000012', '31000000-0000-0000-0000-000000000003', 400, 'Who was the first President of the United States?', 'George Washington', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000013', '31000000-0000-0000-0000-000000000003', 600, 'Which ancient civilization built the pyramids of Giza?', 'Ancient Egyptians', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000014', '31000000-0000-0000-0000-000000000003', 800, 'Who was assassinated in 1914, triggering WWI?', 'Archduke Franz Ferdinand', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000015', '31000000-0000-0000-0000-000000000003', 1000, 'Watch this historical archival clip. Which event is depicted?', 'Moon Landing', 'VIDEO', 'https://example.com/media/apollo11.mp4', true);

-- Questions (Literature)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000016', '31000000-0000-0000-0000-000000000004', 200, 'Who wrote "Romeo and Juliet"?', 'William Shakespeare', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000017', '31000000-0000-0000-0000-000000000004', 400, 'What is the name of the captain in "Moby-Dick"?', 'Captain Ahab', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000018', '31000000-0000-0000-0000-000000000004', 600, 'Which dystopian novel features Big Brother?', '1984', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000019', '31000000-0000-0000-0000-000000000004', 800, 'Who wrote "Pride and Prejudice"?', 'Jane Austen', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000020', '31000000-0000-0000-0000-000000000004', 1000, 'What epic poem tells the story of Odysseus'' journey home?', 'The Odyssey', 'TEXT', NULL, false);

-- Questions (Technology)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000021', '31000000-0000-0000-0000-000000000005', 200, 'What does CPU stand for?', 'Central Processing Unit', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000022', '31000000-0000-0000-0000-000000000005', 400, 'Who co-founded Apple alongside Steve Jobs?', 'Steve Wozniak', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000023', '31000000-0000-0000-0000-000000000005', 600, 'Which programming language was created by James Gosling at Sun Microsystems?', 'Java', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000024', '31000000-0000-0000-0000-000000000005', 800, 'What year was the World Wide Web made publicly available?', '1991', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000025', '31000000-0000-0000-0000-000000000005', 1000, 'What is the main logo/mascot shown here for the Linux operating system kernel?', 'Tux the Penguin', 'IMAGE', 'https://example.com/media/tux.png', false);

-- Questions (Sports)
INSERT INTO questions (id, category_id, points, question_text, answer_text, answer_type, media_url, is_daily_double) VALUES
                                                                                                                         ('31100000-0000-0000-0000-000000000026', '31000000-0000-0000-0000-000000000006', 200, 'How many players are on the field for one team in a soccer match?', '11', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000027', '31000000-0000-0000-0000-000000000006', 400, 'Which country won the FIFA World Cup in 2022?', 'Argentina', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000028', '31000000-0000-0000-0000-000000000006', 600, 'In tennis, what term describes a score of zero?', 'Love', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000029', '31000000-0000-0000-0000-000000000006', 800, 'Which athlete has won the most Olympic gold medals of all time?', 'Michael Phelps', 'TEXT', NULL, false),
                                                                                                                         ('31100000-0000-0000-0000-000000000030', '31000000-0000-0000-0000-000000000006', 1000, 'How long is a standard marathon in miles?', '26.2 miles', 'TEXT', NULL, false);