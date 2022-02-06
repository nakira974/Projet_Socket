-- Author nakira974
-- phpMyAdmin SQL Dump
-- version 4.7.9
-- https://www.phpmyadmin.net/
--
-- Host: mysql-serveur
-- Generation Time: Feb 22, 2021 at 10:35 PM
-- Server version: 10.5.8-MariaDB
-- PHP Version: 7.2.29

SET
SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET
AUTOCOMMIT = 0;
START TRANSACTION;
SET
time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `serveur_db`
--
CREATE
DATABASE IF NOT EXISTS `serveur_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE
`serveur_db`;

-- --------------------------------------------------------

--
-- Table structure for table `groupes`
--

DROP TABLE IF EXISTS `groupes`;
CREATE TABLE IF NOT EXISTS `groupes`
(
    `groupe_uuid` int
(
    11
) NOT NULL AUTO_INCREMENT,
    `nom` varchar
(
    512
) NOT NULL,
    `administrator` int
(
    11
) NOT NULL DEFAULT 11,
    PRIMARY KEY
(
    `groupe_uuid`
),
    UNIQUE KEY `groupes_administrator_uindex`
(
    `administrator`
),
    UNIQUE KEY `groupes_nom_uindex`
(
    `nom`
)
    ) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4;

-- --------------------------------------------------------

--
-- Table structure for table `GroupesMembres`
--

DROP TABLE IF EXISTS `GroupesMembres`;
CREATE TABLE IF NOT EXISTS `GroupesMembres`
(
    `membre_uuid` int
(
    11
) NOT NULL AUTO_INCREMENT,
    `groupe` int
(
    11
) NOT NULL,
    `membre` int
(
    11
) NOT NULL,
    PRIMARY KEY
(
    `membre_uuid`
),
    KEY `GroupesMembres_groupes_groupe_uuid_fk`
(
    `groupe`
),
    KEY `GroupesMembres_users_user_uuid_fk`
(
    `membre`
)
    ) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4;

--
-- Triggers `GroupesMembres`
--
DROP TRIGGER IF EXISTS `MembreDuplicate`;
DELIMITER
$$
CREATE TRIGGER `MembreDuplicate`
    BEFORE INSERT
    ON `GroupesMembres`
    FOR EACH ROW
BEGIN
    IF (
    SELECT COUNT(membre)
    FROM GroupesMembres
    WHERE groupe = NEW.groupe
      AND membre = NEW.membre)  > 0 THEN
        signal sqlstate '45000'
    set message_text ='Erreurr! Utilisateur déjà dans le groupe !';
END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users`
(
    `user_uuid` int
(
    11
) NOT NULL AUTO_INCREMENT,
    `pseudo` varchar
(
    512
) DEFAULT NULL,
    `password` varchar
(
    255
) NOT NULL,
    `email` varchar
(
    255
) NOT NULL,
    `dt_last_connection` date DEFAULT NULL,
    `isConnected` tinyint
(
    1
) NOT NULL,
    PRIMARY KEY
(
    `user_uuid`
),
    UNIQUE KEY `user_uuid_uindex`
(
    `user_uuid`
),
    UNIQUE KEY `user_pseudo`
(
    `pseudo`
)
    ) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_uuid`, `pseudo`, `password`, `email`, `dt_last_connection`, `isConnected`)
VALUES (36, 'f2b0c1c1b30125e247f520eebfaae6e4', 'U2FsdGVkX1807J51BelY6KEsGw3REZBjvKEQcbETEdQ=',
        'nakiraranger@gmail.com', NULL, 1),
       (37, 'c8857c26bc5db685812b7a91b3c5471d', 'U2FsdGVkX1+oHcQJxt01cms0TJ0U4CSze3rCM50f3e0=', 'mrweefle@gmail.com',
        NULL, 1);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `groupes`
--
ALTER TABLE `groupes`
    ADD CONSTRAINT `groupes_users_user_uuid_fk` FOREIGN KEY (`administrator`) REFERENCES `users` (`user_uuid`);

--
-- Constraints for table `GroupesMembres`
--
ALTER TABLE `GroupesMembres`
    ADD CONSTRAINT `GroupesMembres_groupes_groupe_uuid_fk` FOREIGN KEY (`groupe`) REFERENCES `groupes` (`groupe_uuid`),
  ADD CONSTRAINT `GroupesMembres_users_user_uuid_fk` FOREIGN KEY (`membre`) REFERENCES `users` (`user_uuid`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
