DROP TABLE IF EXISTS `account`;
CREATE TABLE `account`
(
    `id`                  bigint NOT NULL AUTO_INCREMENT,
    `created`             datetime(6) DEFAULT CURRENT_TIMESTAMP,
    `updated`             datetime(6) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `modified_by`         varchar(255) DEFAULT NULL,
    `account_expired`     bit(1)       DEFAULT NULL,
    `account_locked`      bit(1)       DEFAULT NULL,
    `role`                tinyint      DEFAULT NULL,
    `credentials_expired` bit(1)       DEFAULT NULL,
    `enabled`             bit(1)       DEFAULT NULL,
    `address1`            varchar(255) DEFAULT NULL,
    `address2`            varchar(255) DEFAULT NULL,
    `city`                varchar(255) DEFAULT NULL,
    `email`               varchar(255) DEFAULT NULL,
    `first_name`          varchar(255) DEFAULT NULL,
    `last_name`           varchar(255) DEFAULT NULL,
    `password`            varchar(255) DEFAULT NULL,
    `state`               varchar(255) DEFAULT NULL,
    `username`            varchar(255) DEFAULT NULL,
    `zip`                 varchar(255) DEFAULT NULL,
    `accepted_eula`       bit(1)       DEFAULT NULL,
    `accepted_pa`         bit(1)       DEFAULT NULL,
    `accepted_tc`         bit(1)       DEFAULT NULL,
    PRIMARY KEY (`id`),
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



DROP TABLE IF EXISTS `permission`;
CREATE TABLE `permission`
(
    `id`          bigint NOT NULL AUTO_INCREMENT,
    `created`     datetime(6) DEFAULT CURRENT_TIMESTAMP,
    `updated`     datetime(6) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `modified_by` varchar(255) DEFAULT NULL,
    `name`        varchar(255) DEFAULT NULL,
    `description` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



DROP TABLE IF EXISTS `account_permission`;
CREATE TABLE `account_permission`
(
    `account_id`    bigint NOT NULL,
    `permission_id` bigint NOT NULL,
    KEY             `FK4pl4ktiq7hgfchxntsjyj4uco` (`permission_id`),
    KEY             `FKcd0ncle1mucnx6xouis6glo94` (`account_id`),
    CONSTRAINT `FK4pl4ktiq7hgfchxntsjyj4uco` FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`),
    CONSTRAINT `FKcd0ncle1mucnx6xouis6glo94` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;



DROP TABLE IF EXISTS `authorization`;
CREATE TABLE `authorization`
(
    `id`                   bigint NOT NULL AUTO_INCREMENT,
    `created`              datetime(6) DEFAULT CURRENT_TIMESTAMP,
    `updated`              datetime(6) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `modified_by`          varchar(255) DEFAULT NULL,
    `access_token`         text,
    `refresh_token`        text,
    `id_token`             text,
    `authorization_guid`   varchar(255) DEFAULT NULL,
    `account_id`           bigint       DEFAULT NULL,
    `registered_client_id` varchar(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `UK_6skwwscpc59tf98u4chpkq9p6` (`account_id`),
    UNIQUE KEY `UK_pdp5lsf32g1rvjhvis38mf48p` (`registered_client_id`),
    CONSTRAINT `FK423r1qxlu7rskqcsghwn40fga` FOREIGN KEY (`account_id`) REFERENCES `account` (`id`),
    CONSTRAINT `FKd6y3hjkl6yf6u7sud8ry6oxfr` FOREIGN KEY (`registered_client_id`) REFERENCES `oauth2_registered_client` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;



DROP TABLE IF EXISTS `oauth2_registered_client`;
CREATE TABLE `oauth2_registered_client`
(
    `id`                            varchar(255) DEFAULT (uuid()),
    `created`                       datetime(6) DEFAULT CURRENT_TIMESTAMP,
    `updated`                       datetime(6) DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `modified_by`                   varchar(255) NOT NULL,
    `client_id`                     varchar(255) DEFAULT NULL,
    `client_name`                   varchar(255) DEFAULT NULL,
    `client_secret`                 varchar(255) DEFAULT NULL,
    `client_id_issued_at`           datetime(6) DEFAULT NULL,
    `client_secret_expires_at`      datetime(6) DEFAULT NULL,
    `authorization_grant_types`     text,
    `client_authentication_methods` text,
    `client_settings`               text,
    `token_settings`                text,
    `redirect_uris`                 text,
    `post_logout_redirect_uris`     text,
    `scopes`                        text,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;