-- Bible O/X Quiz Stages (66권)
INSERT INTO bible_ox_stage (id, stage_number, book_name, created_at, updated_at)
VALUES
-- 구약 (39권)
(1, 1, '창세기', NOW(), NOW()),
(2, 2, '출애굽기', NOW(), NOW()),
(3, 3, '레위기', NOW(), NOW()),
(4, 4, '민수기', NOW(), NOW()),
(5, 5, '신명기', NOW(), NOW()),
(6, 6, '여호수아', NOW(), NOW()),
(7, 7, '사사기', NOW(), NOW()),
(8, 8, '룻기', NOW(), NOW()),
(9, 9, '사무엘상', NOW(), NOW()),
(10, 10, '사무엘하', NOW(), NOW()),
(11, 11, '열왕기상', NOW(), NOW()),
(12, 12, '열왕기하', NOW(), NOW()),
(13, 13, '역대상', NOW(), NOW()),
(14, 14, '역대하', NOW(), NOW()),
(15, 15, '에스라', NOW(), NOW()),
(16, 16, '느헤미야', NOW(), NOW()),
(17, 17, '에스더', NOW(), NOW()),
(18, 18, '욥기', NOW(), NOW()),
(19, 19, '시편', NOW(), NOW()),
(20, 20, '잠언', NOW(), NOW()),
(21, 21, '전도서', NOW(), NOW()),
(22, 22, '아가', NOW(), NOW()),
(23, 23, '이사야', NOW(), NOW()),
(24, 24, '예레미야', NOW(), NOW()),
(25, 25, '예레미야애가', NOW(), NOW()),
(26, 26, '에스겔', NOW(), NOW()),
(27, 27, '다니엘', NOW(), NOW()),
(28, 28, '호세아', NOW(), NOW()),
(29, 29, '요엘', NOW(), NOW()),
(30, 30, '아모스', NOW(), NOW()),
(31, 31, '오바댜', NOW(), NOW()),
(32, 32, '요나', NOW(), NOW()),
(33, 33, '미가', NOW(), NOW()),
(34, 34, '나훔', NOW(), NOW()),
(35, 35, '하박국', NOW(), NOW()),
(36, 36, '스바냐', NOW(), NOW()),
(37, 37, '학개', NOW(), NOW()),
(38, 38, '스가랴', NOW(), NOW()),
(39, 39, '말라기', NOW(), NOW()),
-- 신약 (27권)
(40, 40, '마태복음', NOW(), NOW()),
(41, 41, '마가복음', NOW(), NOW()),
(42, 42, '누가복음', NOW(), NOW()),
(43, 43, '요한복음', NOW(), NOW()),
(44, 44, '사도행전', NOW(), NOW()),
(45, 45, '로마서', NOW(), NOW()),
(46, 46, '고린도전서', NOW(), NOW()),
(47, 47, '고린도후서', NOW(), NOW()),
(48, 48, '갈라디아서', NOW(), NOW()),
(49, 49, '에베소서', NOW(), NOW()),
(50, 50, '빌립보서', NOW(), NOW()),
(51, 51, '골로새서', NOW(), NOW()),
(52, 52, '데살로니가전서', NOW(), NOW()),
(53, 53, '데살로니가후서', NOW(), NOW()),
(54, 54, '디모데전서', NOW(), NOW()),
(55, 55, '디모데후서', NOW(), NOW()),
(56, 56, '디도서', NOW(), NOW()),
(57, 57, '빌레몬서', NOW(), NOW()),
(58, 58, '히브리서', NOW(), NOW()),
(59, 59, '야고보서', NOW(), NOW()),
(60, 60, '베드로전서', NOW(), NOW()),
(61, 61, '베드로후서', NOW(), NOW()),
(62, 62, '요한일서', NOW(), NOW()),
(63, 63, '요한이서', NOW(), NOW()),
(64, 64, '요한삼서', NOW(), NOW()),
(65, 65, '유다서', NOW(), NOW()),
(66, 66, '요한계시록', NOW(), NOW());

-- Stage 1: 창세기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(1, 1, '하나님이 천지를 창조하신 기간은 6일이다.', true, 'EASY', 1, NOW(), NOW()),
(2, 1, '아담은 에덴동산에서 창조되었다.', true, 'EASY', 2, NOW(), NOW()),
(3, 1, '가인은 아벨의 형이다.', true, 'EASY', 3, NOW(), NOW()),
(4, 1, '노아의 방주에는 각 동물이 세 쌍씩 들어갔다.', false, 'NORMAL', 4, NOW(), NOW()),
(5, 1, '바벨탑 사건 후 인류의 언어가 혼잡해졌다.', true, 'EASY', 5, NOW(), NOW()),
(6, 1, '아브라함의 원래 이름은 아브람이었다.', true, 'NORMAL', 6, NOW(), NOW()),
(7, 1, '이삭은 아브라함과 하갈 사이에서 태어났다.', false, 'EASY', 7, NOW(), NOW()),
(8, 1, '야곱은 에서에게 팥죽 한 그릇에 장자권을 팔았다.', false, 'NORMAL', 8, NOW(), NOW()),
(9, 1, '요셉은 야곱의 12아들 중 막내였다.', false, 'NORMAL', 9, NOW(), NOW()),
(10, 1, '요셉은 애굽의 총리가 되었다.', true, 'EASY', 10, NOW(), NOW());

-- Stage 2: 출애굽기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(11, 2, '모세는 바로의 궁에서 자랐다.', true, 'EASY', 1, NOW(), NOW()),
(12, 2, '모세는 불타는 떨기나무에서 하나님을 만났다.', true, 'EASY', 2, NOW(), NOW()),
(13, 2, '애굽에 내린 재앙은 총 7가지였다.', false, 'NORMAL', 3, NOW(), NOW()),
(14, 2, '유월절은 어린양의 피를 문설주에 바르는 것에서 시작되었다.', true, 'EASY', 4, NOW(), NOW()),
(15, 2, '이스라엘 백성은 홍해를 배로 건넜다.', false, 'EASY', 5, NOW(), NOW()),
(16, 2, '십계명은 시내산에서 주어졌다.', true, 'EASY', 6, NOW(), NOW()),
(17, 2, '황금 송아지를 만든 것은 아론이었다.', true, 'NORMAL', 7, NOW(), NOW()),
(18, 2, '만나는 하늘에서 내린 양식이다.', true, 'EASY', 8, NOW(), NOW()),
(19, 2, '모세는 반석을 두 번 쳐서 물을 냈다.', true, 'HARD', 9, NOW(), NOW()),
(20, 2, '성막 건축 지시는 출애굽기에 기록되어 있다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 3: 레위기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(21, 3, '레위기는 제사와 율법에 관한 책이다.', true, 'EASY', 1, NOW(), NOW()),
(22, 3, '번제는 제물 전체를 불에 태우는 제사이다.', true, 'NORMAL', 2, NOW(), NOW()),
(23, 3, '속죄일은 매년 두 번 지켰다.', false, 'NORMAL', 3, NOW(), NOW()),
(24, 3, '레위인은 제사장 직분을 담당했다.', true, 'EASY', 4, NOW(), NOW()),
(25, 3, '희년은 50년마다 돌아온다.', true, 'NORMAL', 5, NOW(), NOW()),
(26, 3, '레위기에는 음식 정결법이 포함되어 있다.', true, 'EASY', 6, NOW(), NOW()),
(27, 3, '피는 생명이므로 먹지 말라고 명령하셨다.', true, 'NORMAL', 7, NOW(), NOW()),
(28, 3, '화목제는 하나님과 화해하는 제사이다.', true, 'NORMAL', 8, NOW(), NOW()),
(29, 3, '레위기의 저자는 다윗이다.', false, 'EASY', 9, NOW(), NOW()),
(30, 3, '나병 환자의 정결 의식이 레위기에 기록되어 있다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 4: 민수기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(31, 4, '민수기는 이스라엘 백성의 인구 조사로 시작한다.', true, 'EASY', 1, NOW(), NOW()),
(32, 4, '이스라엘 백성은 광야에서 40년을 방황했다.', true, 'EASY', 2, NOW(), NOW()),
(33, 4, '가나안 땅을 정탐한 정탐꾼은 10명이었다.', false, 'NORMAL', 3, NOW(), NOW()),
(34, 4, '여호수아와 갈렙만이 가나안 땅에 들어갈 수 있었다.', true, 'EASY', 4, NOW(), NOW()),
(35, 4, '모세는 반석을 한 번 쳐서 물을 냈다.', false, 'HARD', 5, NOW(), NOW()),
(36, 4, '불뱀에게 물린 백성은 놋뱀을 바라보면 살았다.', true, 'NORMAL', 6, NOW(), NOW()),
(37, 4, '발람은 이스라엘을 저주하려 했으나 축복하게 되었다.', true, 'NORMAL', 7, NOW(), NOW()),
(38, 4, '발람의 나귀가 말을 했다.', true, 'EASY', 8, NOW(), NOW()),
(39, 4, '고라의 반역으로 땅이 갈라져 반역자들을 삼켰다.', true, 'NORMAL', 9, NOW(), NOW()),
(40, 4, '아론의 지팡이에서 포도가 열렸다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 5: 신명기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(41, 5, '신명기는 "두 번째 율법"이라는 뜻이다.', true, 'NORMAL', 1, NOW(), NOW()),
(42, 5, '신명기는 모세의 마지막 설교를 담고 있다.', true, 'EASY', 2, NOW(), NOW()),
(43, 5, '쉐마(Shema)는 신명기 6장에 기록되어 있다.', true, 'NORMAL', 3, NOW(), NOW()),
(44, 5, '모세는 가나안 땅에 들어갔다.', false, 'EASY', 4, NOW(), NOW()),
(45, 5, '모세는 느보산에서 가나안 땅을 바라보았다.', true, 'EASY', 5, NOW(), NOW()),
(46, 5, '십계명이 신명기에도 기록되어 있다.', true, 'NORMAL', 6, NOW(), NOW()),
(47, 5, '모세는 120세에 죽었다.', true, 'NORMAL', 7, NOW(), NOW()),
(48, 5, '모세의 무덤 위치는 알려져 있다.', false, 'HARD', 8, NOW(), NOW()),
(49, 5, '신명기에는 축복과 저주에 관한 내용이 있다.', true, 'EASY', 9, NOW(), NOW()),
(50, 5, '여호수아가 모세의 후계자로 임명되었다.', true, 'EASY', 10, NOW(), NOW());

-- Stage 6: 여호수아 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(51, 6, '여호수아는 모세의 후계자이다.', true, 'EASY', 1, NOW(), NOW()),
(52, 6, '이스라엘 백성은 요단강을 배로 건넜다.', false, 'EASY', 2, NOW(), NOW()),
(53, 6, '여리고 성은 이스라엘 백성이 7일 동안 돌았다.', true, 'EASY', 3, NOW(), NOW()),
(54, 6, '라합은 정탐꾼들을 숨겨주었다.', true, 'EASY', 4, NOW(), NOW()),
(55, 6, '아간은 여리고에서 금을 훔쳤다.', true, 'NORMAL', 5, NOW(), NOW()),
(56, 6, '여호수아는 해와 달을 멈추게 했다.', true, 'NORMAL', 6, NOW(), NOW()),
(57, 6, '가나안 땅은 12지파에게 분배되었다.', true, 'EASY', 7, NOW(), NOW()),
(58, 6, '여호수아는 110세에 죽었다.', true, 'HARD', 8, NOW(), NOW()),
(59, 6, '기브온 사람들은 이스라엘을 속여 화친을 맺었다.', true, 'NORMAL', 9, NOW(), NOW()),
(60, 6, '여호수아서의 마지막 말씀은 "오직 여호와만 섬기라"이다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 7: 사사기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(61, 7, '사사기는 이스라엘에 왕이 없던 시대를 다룬다.', true, 'EASY', 1, NOW(), NOW()),
(62, 7, '드보라는 이스라엘의 유일한 여자 사사였다.', true, 'NORMAL', 2, NOW(), NOW()),
(63, 7, '기드온은 300명의 용사로 미디안을 물리쳤다.', true, 'EASY', 3, NOW(), NOW()),
(64, 7, '삼손의 힘의 비밀은 그의 머리카락에 있었다.', true, 'EASY', 4, NOW(), NOW()),
(65, 7, '삼손은 블레셋 사람이었다.', false, 'EASY', 5, NOW(), NOW()),
(66, 7, '입다는 자기 딸을 제물로 바쳤다.', true, 'HARD', 6, NOW(), NOW()),
(67, 7, '에훗은 왼손잡이 사사였다.', true, 'NORMAL', 7, NOW(), NOW()),
(68, 7, '삼손은 당나귀 턱뼈로 1000명을 죽였다.', true, 'NORMAL', 8, NOW(), NOW()),
(69, 7, '들릴라는 삼손의 아내였다.', false, 'NORMAL', 9, NOW(), NOW()),
(70, 7, '사사기에는 "각자 자기 소견에 옳은 대로 행하였다"는 표현이 나온다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 8: 룻기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(71, 8, '룻은 모압 여인이었다.', true, 'EASY', 1, NOW(), NOW()),
(72, 8, '나오미는 룻의 시어머니이다.', true, 'EASY', 2, NOW(), NOW()),
(73, 8, '룻은 나오미와 함께 베들레헴으로 갔다.', true, 'EASY', 3, NOW(), NOW()),
(74, 8, '보아스는 룻의 기업 무를 자였다.', true, 'NORMAL', 4, NOW(), NOW()),
(75, 8, '룻은 보리 추수 때 이삭을 주웠다.', true, 'EASY', 5, NOW(), NOW()),
(76, 8, '오르바는 나오미와 함께 베들레헴으로 갔다.', false, 'NORMAL', 6, NOW(), NOW()),
(77, 8, '룻과 보아스 사이에서 오벳이 태어났다.', true, 'NORMAL', 7, NOW(), NOW()),
(78, 8, '오벳은 다윗의 할아버지이다.', true, 'NORMAL', 8, NOW(), NOW()),
(79, 8, '룻기는 성경에서 가장 짧은 책이다.', false, 'HARD', 9, NOW(), NOW()),
(80, 8, '룻은 예수님의 족보에 포함되어 있다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 9: 사무엘상 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(81, 9, '사무엘은 한나의 아들이다.', true, 'EASY', 1, NOW(), NOW()),
(82, 9, '사무엘은 성전에서 엘리 제사장에게 맡겨졌다.', true, 'EASY', 2, NOW(), NOW()),
(83, 9, '사울은 이스라엘의 첫 번째 왕이다.', true, 'EASY', 3, NOW(), NOW()),
(84, 9, '다윗은 골리앗을 칼로 죽였다.', false, 'EASY', 4, NOW(), NOW()),
(85, 9, '골리앗은 블레셋의 거인 용사였다.', true, 'EASY', 5, NOW(), NOW()),
(86, 9, '요나단은 사울의 아들이며 다윗의 친구였다.', true, 'NORMAL', 6, NOW(), NOW()),
(87, 9, '다윗은 사울을 피해 도망 다녔다.', true, 'EASY', 7, NOW(), NOW()),
(88, 9, '사울은 엔돌의 점쟁이를 찾아갔다.', true, 'HARD', 8, NOW(), NOW()),
(89, 9, '다윗은 사울을 두 번 죽일 기회가 있었지만 죽이지 않았다.', true, 'NORMAL', 9, NOW(), NOW()),
(90, 9, '사울과 요나단은 블레셋과의 전투에서 죽었다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 10: 사무엘하 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(91, 10, '다윗은 예루살렘을 정복하여 수도로 삼았다.', true, 'EASY', 1, NOW(), NOW()),
(92, 10, '다윗은 언약궤를 예루살렘으로 옮겼다.', true, 'EASY', 2, NOW(), NOW()),
(93, 10, '다윗은 밧세바의 남편 우리아를 전쟁터에서 죽게 했다.', true, 'NORMAL', 3, NOW(), NOW()),
(94, 10, '나단 선지자가 다윗의 죄를 책망했다.', true, 'NORMAL', 4, NOW(), NOW()),
(95, 10, '압살롬은 다윗의 아들로 반역을 일으켰다.', true, 'EASY', 5, NOW(), NOW()),
(96, 10, '압살롬은 머리카락이 나무에 걸려 죽었다.', true, 'NORMAL', 6, NOW(), NOW()),
(97, 10, '다윗은 인구 조사를 하여 하나님께 벌을 받았다.', true, 'NORMAL', 7, NOW(), NOW()),
(98, 10, '므비보셋은 요나단의 아들로 다윗의 은혜를 받았다.', true, 'HARD', 8, NOW(), NOW()),
(99, 10, '다윗은 성전을 건축했다.', false, 'EASY', 9, NOW(), NOW()),
(100, 10, '다윗은 이스라엘의 두 번째 왕이다.', true, 'EASY', 10, NOW(), NOW());

-- Stage 11: 열왕기상 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(101, 11, '솔로몬은 다윗의 아들이다.', true, 'EASY', 1, NOW(), NOW()),
(102, 11, '솔로몬은 하나님께 지혜를 구했다.', true, 'EASY', 2, NOW(), NOW()),
(103, 11, '솔로몬은 예루살렘 성전을 건축했다.', true, 'EASY', 3, NOW(), NOW()),
(104, 11, '스바 여왕이 솔로몬의 지혜를 시험하러 왔다.', true, 'NORMAL', 4, NOW(), NOW()),
(105, 11, '솔로몬은 700명의 왕비와 300명의 후궁이 있었다.', true, 'HARD', 5, NOW(), NOW()),
(106, 11, '솔로몬 이후 이스라엘 왕국은 남북으로 분열되었다.', true, 'EASY', 6, NOW(), NOW()),
(107, 11, '엘리야는 바알 선지자들과 갈멜산에서 대결했다.', true, 'NORMAL', 7, NOW(), NOW()),
(108, 11, '엘리야는 까마귀에게 음식을 공급받았다.', true, 'NORMAL', 8, NOW(), NOW()),
(109, 11, '아합은 북이스라엘의 악한 왕이었다.', true, 'NORMAL', 9, NOW(), NOW()),
(110, 11, '이세벨은 아합의 아내로 바알 숭배를 퍼뜨렸다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 12: 열왕기하 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(111, 12, '엘리야는 불 수레를 타고 하늘로 올라갔다.', true, 'EASY', 1, NOW(), NOW()),
(112, 12, '엘리사는 엘리야의 제자이다.', true, 'EASY', 2, NOW(), NOW()),
(113, 12, '엘리사는 엘리야의 갑절의 영감을 구했다.', true, 'NORMAL', 3, NOW(), NOW()),
(114, 12, '나아만 장군은 요단강에서 일곱 번 씻어 나병이 나았다.', true, 'EASY', 4, NOW(), NOW()),
(115, 12, '북이스라엘은 앗수르에 의해 멸망했다.', true, 'NORMAL', 5, NOW(), NOW()),
(116, 12, '남유다는 바벨론에 의해 멸망했다.', true, 'NORMAL', 6, NOW(), NOW()),
(117, 12, '히스기야는 유다의 선한 왕이었다.', true, 'NORMAL', 7, NOW(), NOW()),
(118, 12, '요시야 왕 때 율법책이 발견되었다.', true, 'NORMAL', 8, NOW(), NOW()),
(119, 12, '예루살렘 성전은 바벨론에 의해 파괴되었다.', true, 'EASY', 9, NOW(), NOW()),
(120, 12, '엘리사가 죽은 후 그의 뼈에 닿은 시체가 살아났다.', true, 'HARD', 10, NOW(), NOW());

-- Stage 13: 역대상 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(121, 13, '역대상은 아담의 족보로 시작한다.', true, 'NORMAL', 1, NOW(), NOW()),
(122, 13, '역대상은 주로 다윗 왕의 이야기를 다룬다.', true, 'EASY', 2, NOW(), NOW()),
(123, 13, '다윗은 성전 건축을 위한 재료를 준비했다.', true, 'NORMAL', 3, NOW(), NOW()),
(124, 13, '웃사는 언약궤에 손을 대어 죽었다.', true, 'HARD', 4, NOW(), NOW()),
(125, 13, '역대상에는 레위인의 직무가 상세히 기록되어 있다.', true, 'NORMAL', 5, NOW(), NOW()),
(126, 13, '다윗은 하나님께 성전 건축 허락을 받았다.', false, 'NORMAL', 6, NOW(), NOW()),
(127, 13, '역대상의 저자는 에스라로 추정된다.', true, 'HARD', 7, NOW(), NOW()),
(128, 13, '다윗은 인구 조사를 사탄의 충동으로 시행했다.', true, 'HARD', 8, NOW(), NOW()),
(129, 13, '역대상은 사무엘서와 같은 시대를 다룬다.', true, 'NORMAL', 9, NOW(), NOW()),
(130, 13, '다윗은 솔로몬에게 성전 건축 설계도를 전해주었다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 14: 역대하 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(131, 14, '솔로몬은 역대하에서 성전을 완공했다.', true, 'EASY', 1, NOW(), NOW()),
(132, 14, '솔로몬의 성전 봉헌 기도는 역대하에 기록되어 있다.', true, 'NORMAL', 2, NOW(), NOW()),
(133, 14, '르호보암 때 왕국이 남북으로 분열되었다.', true, 'EASY', 3, NOW(), NOW()),
(134, 14, '아사 왕은 유다의 선한 왕이었다.', true, 'NORMAL', 4, NOW(), NOW()),
(135, 14, '여호사밧은 찬양대를 앞세워 전쟁에서 승리했다.', true, 'NORMAL', 5, NOW(), NOW()),
(136, 14, '웃시야 왕은 교만하여 나병에 걸렸다.', true, 'HARD', 6, NOW(), NOW()),
(137, 14, '히스기야는 앗수르의 침략을 물리쳤다.', true, 'NORMAL', 7, NOW(), NOW()),
(138, 14, '므낫세는 유다에서 가장 악한 왕이었다.', true, 'NORMAL', 8, NOW(), NOW()),
(139, 14, '역대하는 바벨론 포로 귀환 명령으로 끝난다.', true, 'HARD', 9, NOW(), NOW()),
(140, 14, '역대하는 북이스라엘 왕들의 역사를 주로 다룬다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 15: 에스라 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(141, 15, '에스라는 바벨론 포로 귀환 후의 이야기를 다룬다.', true, 'EASY', 1, NOW(), NOW()),
(142, 15, '고레스 왕이 유대인의 귀환을 허락했다.', true, 'EASY', 2, NOW(), NOW()),
(143, 15, '스룹바벨이 첫 번째 귀환을 이끌었다.', true, 'NORMAL', 3, NOW(), NOW()),
(144, 15, '귀환한 유대인들은 성전을 재건했다.', true, 'EASY', 4, NOW(), NOW()),
(145, 15, '에스라는 제사장이자 율법학자였다.', true, 'NORMAL', 5, NOW(), NOW()),
(146, 15, '에스라는 두 번째 귀환을 이끌었다.', true, 'NORMAL', 6, NOW(), NOW()),
(147, 15, '에스라는 이방 여인과의 결혼 문제를 다루었다.', true, 'NORMAL', 7, NOW(), NOW()),
(148, 15, '성전 재건은 방해 없이 순조롭게 진행되었다.', false, 'NORMAL', 8, NOW(), NOW()),
(149, 15, '학개와 스가랴가 성전 재건을 격려했다.', true, 'HARD', 9, NOW(), NOW()),
(150, 15, '에스라는 모세의 율법을 백성에게 읽어주었다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 16: 느헤미야 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(151, 16, '느헤미야는 페르시아 왕의 술 관원이었다.', true, 'NORMAL', 1, NOW(), NOW()),
(152, 16, '느헤미야는 예루살렘 성벽을 재건했다.', true, 'EASY', 2, NOW(), NOW()),
(153, 16, '예루살렘 성벽은 52일 만에 완공되었다.', true, 'NORMAL', 3, NOW(), NOW()),
(154, 16, '산발랏과 도비야가 성벽 재건을 방해했다.', true, 'NORMAL', 4, NOW(), NOW()),
(155, 16, '느헤미야는 백성들이 한 손에 무기를 들고 일하게 했다.', true, 'NORMAL', 5, NOW(), NOW()),
(156, 16, '느헤미야는 유다 총독으로 임명되었다.', true, 'NORMAL', 6, NOW(), NOW()),
(157, 16, '느헤미야는 가난한 백성들의 빚을 탕감하게 했다.', true, 'NORMAL', 7, NOW(), NOW()),
(158, 16, '에스라가 수문 앞에서 율법을 읽었다.', true, 'HARD', 8, NOW(), NOW()),
(159, 16, '느헤미야는 안식일 상거래를 금지했다.', true, 'NORMAL', 9, NOW(), NOW()),
(160, 16, '느헤미야서에는 하나님이라는 단어가 등장하지 않는다.', false, 'HARD', 10, NOW(), NOW());

-- Stage 17: 에스더 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(161, 17, '에스더는 페르시아의 왕비가 되었다.', true, 'EASY', 1, NOW(), NOW()),
(162, 17, '에스더는 유대인이었다.', true, 'EASY', 2, NOW(), NOW()),
(163, 17, '모르드개는 에스더의 삼촌이다.', true, 'NORMAL', 3, NOW(), NOW()),
(164, 17, '하만은 유대인을 멸망시키려는 음모를 꾸몄다.', true, 'EASY', 4, NOW(), NOW()),
(165, 17, '에스더는 왕의 부름 없이 왕 앞에 나아갔다.', true, 'NORMAL', 5, NOW(), NOW()),
(166, 17, '하만은 모르드개를 매달려고 만든 장대에 자신이 매달렸다.', true, 'NORMAL', 6, NOW(), NOW()),
(167, 17, '부림절은 에스더서의 사건을 기념하는 절기이다.', true, 'NORMAL', 7, NOW(), NOW()),
(168, 17, '아하수에로 왕은 바벨론의 왕이었다.', false, 'NORMAL', 8, NOW(), NOW()),
(169, 17, '에스더서에는 하나님이라는 단어가 등장하지 않는다.', true, 'HARD', 9, NOW(), NOW()),
(170, 17, '와스디 왕비가 폐위된 후 에스더가 왕비가 되었다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 18: 욥기 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(171, 18, '욥은 우스 땅에 살았다.', true, 'NORMAL', 1, NOW(), NOW()),
(172, 18, '하나님은 사탄에게 욥을 시험하도록 허락하셨다.', true, 'EASY', 2, NOW(), NOW()),
(173, 18, '욥은 자녀 10명을 모두 잃었다.', true, 'NORMAL', 3, NOW(), NOW()),
(174, 18, '욥의 세 친구는 엘리바스, 빌닷, 소발이다.', true, 'HARD', 4, NOW(), NOW()),
(175, 18, '욥의 친구들은 욥이 죄를 지었기 때문에 고난을 받는다고 주장했다.', true, 'NORMAL', 5, NOW(), NOW()),
(176, 18, '욥은 고난 중에 하나님을 저주했다.', false, 'EASY', 6, NOW(), NOW()),
(177, 18, '엘리후는 욥의 네 번째 친구로 가장 젊었다.', true, 'HARD', 7, NOW(), NOW()),
(178, 18, '하나님은 폭풍 가운데서 욥에게 말씀하셨다.', true, 'NORMAL', 8, NOW(), NOW()),
(179, 18, '욥은 회복 후 이전보다 두 배의 재산을 받았다.', true, 'NORMAL', 9, NOW(), NOW()),
(180, 18, '욥기는 지혜문학에 속한다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 19: 시편 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(181, 19, '시편은 150편으로 구성되어 있다.', true, 'EASY', 1, NOW(), NOW()),
(182, 19, '시편의 대부분은 다윗이 기록했다.', true, 'EASY', 2, NOW(), NOW()),
(183, 19, '시편 23편은 "여호와는 나의 목자시니"로 시작한다.', true, 'EASY', 3, NOW(), NOW()),
(184, 19, '시편은 성경에서 가장 긴 책이다.', true, 'NORMAL', 4, NOW(), NOW()),
(185, 19, '시편 119편은 성경에서 가장 긴 장이다.', true, 'NORMAL', 5, NOW(), NOW()),
(186, 19, '시편 117편은 성경에서 가장 짧은 장이다.', true, 'HARD', 6, NOW(), NOW()),
(187, 19, '시편은 5권으로 나뉘어져 있다.', true, 'HARD', 7, NOW(), NOW()),
(188, 19, '시편 51편은 다윗이 밧세바 사건 후 회개하며 쓴 시이다.', true, 'NORMAL', 8, NOW(), NOW()),
(189, 19, '시편에는 메시아 예언이 포함되어 있다.', true, 'NORMAL', 9, NOW(), NOW()),
(190, 19, '시편은 모두 다윗이 기록했다.', false, 'EASY', 10, NOW(), NOW());

-- Stage 20: 잠언 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(191, 20, '잠언의 대부분은 솔로몬이 기록했다.', true, 'EASY', 1, NOW(), NOW()),
(192, 20, '잠언은 지혜문학에 속한다.', true, 'EASY', 2, NOW(), NOW()),
(193, 20, '"여호와를 경외하는 것이 지식의 근본이니라"는 잠언의 말씀이다.', true, 'EASY', 3, NOW(), NOW()),
(194, 20, '잠언 31장은 현숙한 여인에 대해 기록하고 있다.', true, 'NORMAL', 4, NOW(), NOW()),
(195, 20, '잠언에는 아굴과 르무엘의 말씀도 포함되어 있다.', true, 'HARD', 5, NOW(), NOW()),
(196, 20, '잠언은 일상생활의 실천적 지혜를 다룬다.', true, 'EASY', 6, NOW(), NOW()),
(197, 20, '"네 마음을 다하여 여호와를 신뢰하라"는 잠언의 말씀이다.', true, 'NORMAL', 7, NOW(), NOW()),
(198, 20, '잠언에서 지혜는 여성으로 의인화되어 있다.', true, 'NORMAL', 8, NOW(), NOW()),
(199, 20, '잠언은 31장으로 구성되어 있다.', true, 'NORMAL', 9, NOW(), NOW()),
(200, 20, '잠언은 모두 솔로몬이 기록했다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 21: 전도서 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(201, 21, '전도서의 저자는 솔로몬으로 추정된다.', true, 'EASY', 1, NOW(), NOW()),
(202, 21, '"헛되고 헛되며 헛되고 헛되니 모든 것이 헛되도다"는 전도서의 말씀이다.', true, 'EASY', 2, NOW(), NOW()),
(203, 21, '전도서는 인생의 의미를 탐구하는 책이다.', true, 'EASY', 3, NOW(), NOW()),
(204, 21, '"해 아래 새 것이 없나니"는 전도서의 말씀이다.', true, 'NORMAL', 4, NOW(), NOW()),
(205, 21, '전도서는 12장으로 구성되어 있다.', true, 'NORMAL', 5, NOW(), NOW()),
(206, 21, '"범사에 기한이 있고 천하 만사가 다 때가 있나니"는 전도서의 말씀이다.', true, 'NORMAL', 6, NOW(), NOW()),
(207, 21, '전도서의 결론은 "하나님을 경외하고 그의 명령들을 지키라"이다.', true, 'NORMAL', 7, NOW(), NOW()),
(208, 21, '전도서는 지혜문학에 속한다.', true, 'EASY', 8, NOW(), NOW()),
(209, 21, '"청년의 때에 창조주를 기억하라"는 전도서의 말씀이다.', true, 'NORMAL', 9, NOW(), NOW()),
(210, 21, '전도서는 낙관적인 인생관을 주로 표현한다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 22: 아가 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(211, 22, '아가는 솔로몬의 아가라고도 불린다.', true, 'EASY', 1, NOW(), NOW()),
(212, 22, '아가는 사랑의 노래를 담은 책이다.', true, 'EASY', 2, NOW(), NOW()),
(213, 22, '아가는 8장으로 구성되어 있다.', true, 'NORMAL', 3, NOW(), NOW()),
(214, 22, '아가는 신랑과 신부의 대화 형식으로 되어 있다.', true, 'NORMAL', 4, NOW(), NOW()),
(215, 22, '아가는 하나님과 이스라엘(또는 그리스도와 교회)의 관계를 상징한다고 해석된다.', true, 'NORMAL', 5, NOW(), NOW()),
(216, 22, '아가는 구약성경의 지혜문학에 속한다.', true, 'NORMAL', 6, NOW(), NOW()),
(217, 22, '"사랑은 죽음같이 강하고"는 아가의 말씀이다.', true, 'NORMAL', 7, NOW(), NOW()),
(218, 22, '아가는 유월절에 읽는 두루마리 중 하나이다.', true, 'HARD', 8, NOW(), NOW()),
(219, 22, '아가에는 하나님의 이름이 직접 언급된다.', false, 'HARD', 9, NOW(), NOW()),
(220, 22, '아가는 성경에서 가장 짧은 책이다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 23: 이사야 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(221, 23, '이사야는 대선지서에 속한다.', true, 'EASY', 1, NOW(), NOW()),
(222, 23, '이사야는 66장으로 구성되어 있다.', true, 'NORMAL', 2, NOW(), NOW()),
(223, 23, '이사야 53장은 고난받는 종에 대한 예언이다.', true, 'EASY', 3, NOW(), NOW()),
(224, 23, '"보라 처녀가 잉태하여 아들을 낳을 것이요"는 이사야의 예언이다.', true, 'NORMAL', 4, NOW(), NOW()),
(225, 23, '이사야는 웃시야 왕이 죽던 해에 소명을 받았다.', true, 'NORMAL', 5, NOW(), NOW()),
(226, 23, '이사야는 "거룩하다 거룩하다 거룩하다 만군의 여호와여"라는 천사의 찬양을 들었다.', true, 'NORMAL', 6, NOW(), NOW()),
(227, 23, '"임마누엘"이라는 이름은 이사야서에 처음 등장한다.', true, 'HARD', 7, NOW(), NOW()),
(228, 23, '이사야 40장부터는 위로의 메시지가 주를 이룬다.', true, 'NORMAL', 8, NOW(), NOW()),
(229, 23, '이사야는 북이스라엘의 선지자였다.', false, 'NORMAL', 9, NOW(), NOW()),
(230, 23, '이사야는 메시아에 대한 예언이 가장 많은 선지서이다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 24: 예레미야 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(231, 24, '예레미야는 "눈물의 선지자"로 불린다.', true, 'EASY', 1, NOW(), NOW()),
(232, 24, '예레미야는 대선지서에 속한다.', true, 'EASY', 2, NOW(), NOW()),
(233, 24, '예레미야는 요시야 왕 때부터 사역을 시작했다.', true, 'NORMAL', 3, NOW(), NOW()),
(234, 24, '예레미야는 바룩에게 자신의 예언을 기록하게 했다.', true, 'NORMAL', 4, NOW(), NOW()),
(235, 24, '예레미야는 결혼하지 말라는 명령을 받았다.', true, 'HARD', 5, NOW(), NOW()),
(236, 24, '예레미야는 바벨론에 항복하라고 선포했다.', true, 'NORMAL', 6, NOW(), NOW()),
(237, 24, '예레미야는 옥에 갇히고 구덩이에 던져지는 고난을 당했다.', true, 'NORMAL', 7, NOW(), NOW()),
(238, 24, '"새 언약"에 대한 예언이 예레미야서에 있다.', true, 'NORMAL', 8, NOW(), NOW()),
(239, 24, '예레미야는 예루살렘 멸망 후 애굽으로 끌려갔다.', true, 'HARD', 9, NOW(), NOW()),
(240, 24, '예레미야는 북이스라엘의 선지자였다.', false, 'EASY', 10, NOW(), NOW());

-- Stage 25: 예레미야애가 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(241, 25, '예레미야애가는 예루살렘의 멸망을 애도하는 책이다.', true, 'EASY', 1, NOW(), NOW()),
(242, 25, '예레미야애가의 저자는 예레미야로 추정된다.', true, 'EASY', 2, NOW(), NOW()),
(243, 25, '예레미야애가는 5장으로 구성되어 있다.', true, 'NORMAL', 3, NOW(), NOW()),
(244, 25, '예레미야애가는 히브리어 알파벳 순서의 애가 형식(아크로스틱)을 따른다.', true, 'HARD', 4, NOW(), NOW()),
(245, 25, '"여호와의 인자와 긍휼이 무궁하시도다"는 예레미야애가의 말씀이다.', true, 'NORMAL', 5, NOW(), NOW()),
(246, 25, '예레미야애가는 바벨론 포로기에 기록되었다.', true, 'NORMAL', 6, NOW(), NOW()),
(247, 25, '예레미야애가는 유대인들이 아브월 9일에 읽는다.', true, 'HARD', 7, NOW(), NOW()),
(248, 25, '"주의 인자하심이 아침마다 새로우니"는 예레미야애가의 말씀이다.', true, 'NORMAL', 8, NOW(), NOW()),
(249, 25, '예레미야애가는 희망의 메시지 없이 끝난다.', false, 'NORMAL', 9, NOW(), NOW()),
(250, 25, '예레미야애가는 대선지서에 속한다.', false, 'NORMAL', 10, NOW(), NOW());

-- Stage 40: 마태복음 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(391, 40, '예수님은 베들레헴에서 태어나셨다.', true, 'EASY', 1, NOW(), NOW()),
(392, 40, '동방박사는 12명이었다.', false, 'NORMAL', 2, NOW(), NOW()),
(393, 40, '예수님은 요단강에서 세례 요한에게 세례를 받으셨다.', true, 'EASY', 3, NOW(), NOW()),
(394, 40, '산상수훈은 마태복음에 기록되어 있다.', true, 'EASY', 4, NOW(), NOW()),
(395, 40, '예수님의 열두 제자 중 첫 번째로 부르신 사람은 베드로이다.', false, 'HARD', 5, NOW(), NOW()),
(396, 40, '오병이어 기적에서 남은 조각은 12바구니였다.', true, 'NORMAL', 6, NOW(), NOW()),
(397, 40, '예수님을 배반한 제자는 가룟 유다이다.', true, 'EASY', 7, NOW(), NOW()),
(398, 40, '예수님은 십자가에서 3시간 동안 달려 계셨다.', false, 'HARD', 8, NOW(), NOW()),
(399, 40, '예수님은 부활 후 40일 동안 지상에 계셨다.', true, 'NORMAL', 9, NOW(), NOW()),
(400, 40, '대위임령은 마태복음의 마지막 장에 기록되어 있다.', true, 'NORMAL', 10, NOW(), NOW());

-- Stage 43: 요한복음 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(421, 43, '요한복음은 "태초에 말씀이 계시니라"로 시작한다.', true, 'EASY', 1, NOW(), NOW()),
(422, 43, '예수님의 첫 번째 이적은 물을 포도주로 바꾸신 것이다.', true, 'EASY', 2, NOW(), NOW()),
(423, 43, '니고데모는 밤에 예수님을 찾아왔다.', true, 'NORMAL', 3, NOW(), NOW()),
(424, 43, '예수님은 스스로 "나는 길이요 진리요 생명이라"고 말씀하셨다.', true, 'EASY', 4, NOW(), NOW()),
(425, 43, '나사로는 죽은 지 이틀 만에 살아났다.', false, 'NORMAL', 5, NOW(), NOW()),
(426, 43, '요한복음에는 예수님의 "나는 ~이다" 선언이 7개 있다.', true, 'HARD', 6, NOW(), NOW()),
(427, 43, '최후의 만찬 때 예수님은 제자들의 발을 씻기셨다.', true, 'EASY', 7, NOW(), NOW()),
(428, 43, '예수님은 도마에게 "믿음 없는 자가 되지 말고 믿는 자가 되라"고 말씀하셨다.', true, 'NORMAL', 8, NOW(), NOW()),
(429, 43, '요한복음의 저자는 세베대의 아들 요한이다.', true, 'NORMAL', 9, NOW(), NOW()),
(430, 43, '요한복음 3:16은 성경에서 가장 유명한 구절 중 하나이다.', true, 'EASY', 10, NOW(), NOW());

-- Stage 66: 요한계시록 (10문제)
INSERT INTO bible_ox_question (id, stage_id, question_text, correct_answer, difficulty, order_index, created_at, updated_at)
VALUES
(651, 66, '요한계시록은 성경의 마지막 책이다.', true, 'EASY', 1, NOW(), NOW()),
(652, 66, '요한계시록은 밧모섬에서 기록되었다.', true, 'NORMAL', 2, NOW(), NOW()),
(653, 66, '요한계시록에는 7개의 교회에 보내는 편지가 있다.', true, 'EASY', 3, NOW(), NOW()),
(654, 66, '666은 짐승의 수이다.', true, 'EASY', 4, NOW(), NOW()),
(655, 66, '새 예루살렘에는 성전이 있다.', false, 'HARD', 5, NOW(), NOW()),
(656, 66, '144,000명은 이스라엘 열두 지파에서 인 맞은 자들이다.', true, 'NORMAL', 6, NOW(), NOW()),
(657, 66, '생명책에 이름이 없는 자는 불못에 던져진다.', true, 'NORMAL', 7, NOW(), NOW()),
(658, 66, '요한계시록의 저자는 사도 바울이다.', false, 'EASY', 8, NOW(), NOW()),
(659, 66, '새 하늘과 새 땅에서는 더 이상 바다가 없다.', true, 'HARD', 9, NOW(), NOW()),
(660, 66, '요한계시록은 "아멘 주 예수여 오시옵소서"로 끝난다.', true, 'NORMAL', 10, NOW(), NOW());
