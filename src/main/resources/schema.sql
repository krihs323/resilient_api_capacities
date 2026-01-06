CREATE TABLE IF NOT EXISTS capacities (
	id int NOT NULL AUTO_INCREMENT,
	name varchar(50) NULL,
	description varchar(90) NULL,
	PRIMARY KEY (`id`)
)
ENGINE=InnoDB
DEFAULT CHARSET=utf8mb4
COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS capacities_x_bootcamps (
  id int NOT NULL AUTO_INCREMENT,
  id_bootcamp int NOT NULL,
  id_capacity int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `capacities_x_tecnologies_unique` (`id_bootcamp`,`id_capacity`)
) ENGINE=InnoDB AUTO_INCREMENT=82 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;