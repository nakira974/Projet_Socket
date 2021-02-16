-- phpMyAdmin SQL Dump
-- version 5.0.2
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : jeu. 11 fév. 2021 à 09:45
-- Version du serveur :  8.0.21
-- Version de PHP : 7.3.21

SET
SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET
time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `serveur_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `user`
--

CREATE
DATABASE IF NOT EXISTS serveur_db;

USE
serveur_db;

DROP TABLE IF EXISTS users;

CREATE TABLE IF NOT EXISTS users
(
    user_uuid tinyint
(
    4
) NOT NULL AUTO_INCREMENT, pseudo varchar
(
    16
) DEFAULT NULL, password varchar
(
    255
) NOT NULL, email varchar
(
    255
) NOT NULL, dt_last_connection date DEFAULT NULL, PRIMARY KEY
(
    user_uuid
) ) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE =utf8mb4_general_ci;

CREATE USER 'ServerMaster'@'%' IDENTIFIED VIA mysql_native_password USING '***';GRANT ALL PRIVILEGES ON *.* TO
'ServerMaster'@'%' REQUIRE NONE WITH GRANT OPTION
MAX_QUERIES_PER_HOUR 0 MAX_CONNECTIONS_PER_HOUR 0
MAX_UPDATES_PER_HOUR 0 MAX_USER_CONNECTIONS 0;
GRANT ALL PRIVILEGES ON `ServerMaster\_%`.* TO 'ServerMaster'@'%';

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
