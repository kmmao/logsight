CREATE TABLE IF NOT EXISTS `log_field`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path` varchar(255) DEFAULT NULL COMMENT 'Log files path',
  `timeline` timestamp COMMENT 'Last update time',
  `file_count` int COMMENT 'File count',
  `create_time` timestamp,
  `status` int default 0,
  PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `log_field_file`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `field_id` int(11),
  `path_name` varchar(255) DEFAULT NULL,
  `file_size` bigint DEFAULT 0,
  `timeline` timestamp COMMENT 'Last update time',
  `status` int DEFAULT 0,
  `last_scan` timestamp ,
  `prev_size` bigint DEFAULT 0,
  PRIMARY KEY (`id`)
);
CREATE TABLE IF NOT EXISTS `report_file`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `path_name` varchar(255) DEFAULT NULL,
  `file_size` bigint DEFAULT 0,
  `timespan` timestamp COMMENT 'Last update time',
  PRIMARY KEY (`id`),
  INDEX path_name_idx ( `path_name` ),
  INDEX field_id_idx ( `field_id` )
);
ALTER TABLE `log_field` ADD UNIQUE (`path`);
ALTER TABLE `log_field_file` ADD UNIQUE (`field_id`,`path_name`);