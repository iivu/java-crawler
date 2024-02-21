CREATE TABLE `news` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `title` VARCHAR(100),
    `content` TEXT,
    `created_at` TIMESTAMP DEFAULT NOW(),
    `updated_at` TIMESTAMP DEFAULT NOW()
);
CREATE TABLE `links_to_be_processed` (
    `link` VARCHAR(200)
);
CREATE TABLE `links_already_processed` (
    `link` VARCHAR(200)
);