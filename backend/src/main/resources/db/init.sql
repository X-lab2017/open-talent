DROP TABLE IF EXISTS `members`;
DROP TABLE IF EXISTS `organizations`;


CREATE TABLE `organizations` (
    organization_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    link VARCHAR(255),
    category1 ENUM('类别1', '类别2', '类别3', '类别4'),
    category2 ENUM('类别1', '类别2', '类别3'),
    category3 ENUM('类别1', '类别2', '类别3'),
    category4 ENUM('类别1', '类别2'),
    start_date DATE,
    end_date DATE,
    contact_email VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE `members` (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    nationality VARCHAR(100),
    organization_id INT,
    github_account VARCHAR(100),
    gitee_account VARCHAR(100),
    atomgit_account VARCHAR(100),
    contact_email VARCHAR(255),
    contact_address VARCHAR(255),
    openrank_value INT,
    community VARCHAR(255),
    active_months INT,
    FOREIGN KEY (organization_id) REFERENCES organizations (organization_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;