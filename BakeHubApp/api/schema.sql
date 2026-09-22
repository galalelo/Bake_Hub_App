CREATE DATABASE IF NOT EXISTS bakehub CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bakehub;

CREATE TABLE IF NOT EXISTS users (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  display_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  skill_level VARCHAR(40) NOT NULL DEFAULT 'Beginner',
  level INT NOT NULL DEFAULT 1,
  xp INT NOT NULL DEFAULT 0,
  streak_count INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS recipes (
  id VARCHAR(32) PRIMARY KEY,
  title VARCHAR(180) NOT NULL,
  author_name VARCHAR(120) NOT NULL,
  source VARCHAR(30) NOT NULL DEFAULT 'BakeHub',
  category VARCHAR(80) NOT NULL,
  difficulty ENUM('EASY','MEDIUM','HARD') NOT NULL DEFAULT 'EASY',
  prep_time_minutes INT NOT NULL DEFAULT 0,
  cook_time_minutes INT NOT NULL DEFAULT 0,
  servings INT NOT NULL DEFAULT 1,
  avg_rating DECIMAL(3,2) NOT NULL DEFAULT 0,
  rating_count INT NOT NULL DEFAULT 0,
  calories INT NOT NULL DEFAULT 0,
  protein_g DECIMAL(6,2) NOT NULL DEFAULT 0,
  carbs_g DECIMAL(6,2) NOT NULL DEFAULT 0,
  fat_g DECIMAL(6,2) NOT NULL DEFAULT 0,
  created_by INT UNSIGNED NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ingredients (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  recipe_id VARCHAR(32) NOT NULL,
  name VARCHAR(180) NOT NULL,
  quantity VARCHAR(40) NOT NULL,
  unit VARCHAR(40) NOT NULL DEFAULT '',
  sort_order INT NOT NULL DEFAULT 0,
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recipe_steps (
  id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
  recipe_id VARCHAR(32) NOT NULL,
  step_number INT NOT NULL,
  title VARCHAR(120) NOT NULL,
  instruction TEXT NOT NULL,
  timer_seconds INT NULL,
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS saved_recipes (
  user_id INT UNSIGNED NOT NULL,
  recipe_id VARCHAR(32) NOT NULL,
  saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, recipe_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
);

INSERT INTO users (display_name, email, password_hash, skill_level, level, xp, streak_count)
VALUES ('Alex Baker', 'demo@bakehub.app', SHA2('bakehub123', 256), 'Intermediate', 4, 1240, 5)
ON DUPLICATE KEY UPDATE email=email;

INSERT INTO recipes (id,title,author_name,source,category,difficulty,prep_time_minutes,cook_time_minutes,servings,avg_rating,rating_count,calories,protein_g,carbs_g,fat_g)
VALUES
('r1','Classic Banana Bread','BakeHub','BakeHub','Bread','EASY',15,50,8,4.80,312,210,4,32,7),
('r2','Chewy Chocolate Chip Cookies','BakeHub','BakeHub','Cookies','EASY',15,10,18,4.90,587,165,2,21,8),
('r3','Beginner Sourdough Loaf','BakeHub','BakeHub','Bread','HARD',30,45,10,4.60,201,180,6,35,1),
('r4','Butter Croissants','Jordan P.','User','Pastries','HARD',90,20,12,4.70,88,270,5,26,16)
ON DUPLICATE KEY UPDATE title=VALUES(title);

INSERT IGNORE INTO ingredients (recipe_id,name,quantity,unit,sort_order) VALUES
('r1','Flour','2','cups',1),('r1','Ripe bananas','3','',2),('r1','Eggs','2','',3),('r1','Sugar','1/2','cup',4),('r1','Butter, melted','1/3','cup',5),('r1','Baking soda','1','tsp',6),
('r2','Flour','2 1/4','cups',1),('r2','Butter, softened','1','cup',2),('r2','Brown sugar','3/4','cup',3),('r2','White sugar','3/4','cup',4),('r2','Eggs','2','',5),('r2','Chocolate chips','2','cups',6),
('r3','Active sourdough starter','100','g',1),('r3','Bread flour','500','g',2),('r3','Water','350','ml',3),('r3','Salt','10','g',4),
('r4','Bread flour','500','g',1),('r4','Butter (for laminating)','300','g',2),('r4','Milk','150','ml',3),('r4','Yeast','10','g',4);

INSERT IGNORE INTO recipe_steps (recipe_id,step_number,title,instruction,timer_seconds) VALUES
('r1',1,'Preheat','Preheat the oven to 175°C and grease a loaf tin.',NULL),('r1',2,'Mash bananas','Mash the bananas in a large bowl until smooth.',NULL),('r1',3,'Cream butter and sugar','Beat butter and sugar together until light and fluffy.',180),('r1',4,'Combine wet ingredients','Mix in the eggs and mashed banana until combined.',NULL),('r1',5,'Fold in dry ingredients','Gently fold in the flour and baking soda until just combined.',NULL),('r1',6,'Bake','Pour into the tin and bake until a skewer comes out clean.',3000),('r1',7,'Cool','Cool in the tin for 10 minutes, then turn out onto a rack.',600),
('r2',1,'Preheat','Preheat the oven to 190°C and line two baking trays.',NULL),('r2',2,'Cream butter and sugars','Beat the butter, brown sugar, and white sugar until fluffy.',180),('r2',3,'Add eggs','Beat in the eggs one at a time.',NULL),('r2',4,'Mix dough','Stir in the flour, then fold through the chocolate chips.',NULL),('r2',5,'Portion','Scoop tablespoon-sized balls of dough onto the trays.',NULL),('r2',6,'Bake','Bake until the edges are golden but centres still look soft.',600),
('r3',1,'Mix','Combine starter, flour, and water; rest for 30 minutes.',1800),('r3',2,'Add salt','Add the salt and work it through the dough.',NULL),('r3',3,'Bulk ferment','Cover and let rise until roughly doubled.',14400),('r3',4,'Shape','Shape the dough into a tight round loaf.',NULL),('r3',5,'Bake','Bake in a preheated Dutch oven until deeply golden.',2700),
('r4',1,'Make dough','Mix flour, milk, and yeast into a smooth dough.',NULL),('r4',2,'Laminate','Fold the butter block into the dough in three turns, chilling between each.',1800),('r4',3,'Shape','Roll out and cut into triangles, then roll into crescents.',NULL),('r4',4,'Prove','Prove until visibly puffy.',5400),('r4',5,'Bake','Bake until deep golden brown.',1200);

INSERT IGNORE INTO saved_recipes (user_id, recipe_id)
SELECT u.id, 'r1' FROM users u WHERE u.email='demo@bakehub.app';
INSERT IGNORE INTO saved_recipes (user_id, recipe_id)
SELECT u.id, 'r3' FROM users u WHERE u.email='demo@bakehub.app';
