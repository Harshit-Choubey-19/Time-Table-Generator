-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: timetable
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `classroom`
--

DROP TABLE IF EXISTS `classroom`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `classroom` (
  `classId` varchar(10) NOT NULL,
  `isLab` tinyint(1) DEFAULT '0',
  `capacity` int DEFAULT NULL,
  `hasAV` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`classId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classroom`
--

LOCK TABLES `classroom` WRITE;
/*!40000 ALTER TABLE `classroom` DISABLE KEYS */;
INSERT INTO `classroom` VALUES ('101',0,45,1),('102',0,150,1),('103',0,45,1),('104',0,150,1),('105',0,150,1),('D111',1,100,1),('D222',1,100,1),('D333',1,30,1),('D444',1,30,1);
/*!40000 ALTER TABLE `classroom` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conflict`
--

DROP TABLE IF EXISTS `conflict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conflict` (
  `conflictId` int NOT NULL AUTO_INCREMENT,
  `userId` varchar(20) DEFAULT NULL,
  `courseCode` varchar(10) DEFAULT NULL,
  `instructId` varchar(10) DEFAULT NULL,
  `classId` varchar(10) DEFAULT NULL,
  `slotId` int DEFAULT NULL,
  `conflictType` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`conflictId`),
  KEY `userId` (`userId`),
  KEY `courseCode` (`courseCode`),
  KEY `instructId` (`instructId`),
  KEY `classId` (`classId`),
  KEY `slotId` (`slotId`),
  CONSTRAINT `conflict_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
  CONSTRAINT `conflict_ibfk_2` FOREIGN KEY (`courseCode`) REFERENCES `course` (`courseCode`),
  CONSTRAINT `conflict_ibfk_3` FOREIGN KEY (`instructId`) REFERENCES `instructor` (`instructId`),
  CONSTRAINT `conflict_ibfk_4` FOREIGN KEY (`classId`) REFERENCES `classroom` (`classId`),
  CONSTRAINT `conflict_ibfk_5` FOREIGN KEY (`slotId`) REFERENCES `timeslot` (`slotId`)
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conflict`
--

LOCK TABLES `conflict` WRITE;
/*!40000 ALTER TABLE `conflict` DISABLE KEYS */;
/*!40000 ALTER TABLE `conflict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `course`
--

DROP TABLE IF EXISTS `course`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `course` (
  `courseCode` varchar(10) NOT NULL,
  `name` varchar(20) NOT NULL,
  `assignedInstructId` varchar(10) DEFAULT NULL,
  `studentCount` int DEFAULT '0',
  `consistLab` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`courseCode`),
  KEY `assignedInstructId` (`assignedInstructId`),
  CONSTRAINT `course_ibfk_1` FOREIGN KEY (`assignedInstructId`) REFERENCES `instructor` (`instructId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `course`
--

LOCK TABLES `course` WRITE;
/*!40000 ALTER TABLE `course` DISABLE KEYS */;
INSERT INTO `course` VALUES ('CE F101','AOS','CE5',0,0),('CE F102','Highway Eng','CE1',0,1),('CE F103','Eng Hydrology','CE3',0,0),('CE F104','CPT','CE2',0,0),('CE F105','Foundation Eng','CE4',0,0),('CE F106','AI in Civil','CE6',0,0),('CE F107','Environmental','CE7',0,0),('CS F101','DSA','CS2',0,1),('CS F102','OS','CS1',0,0),('CS F103','OOPS','CS3',0,1),('CS F104','Machine Learning','CS4',0,0);
/*!40000 ALTER TABLE `course` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courseclass`
--

DROP TABLE IF EXISTS `courseclass`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courseclass` (
  `courseCode` varchar(10) DEFAULT NULL,
  `roomId` varchar(10) DEFAULT NULL,
  KEY `courseCode` (`courseCode`),
  KEY `roomId` (`roomId`),
  CONSTRAINT `courseclass_ibfk_1` FOREIGN KEY (`courseCode`) REFERENCES `course` (`courseCode`),
  CONSTRAINT `courseclass_ibfk_2` FOREIGN KEY (`roomId`) REFERENCES `classroom` (`classId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courseclass`
--

LOCK TABLES `courseclass` WRITE;
/*!40000 ALTER TABLE `courseclass` DISABLE KEYS */;
INSERT INTO `courseclass` VALUES ('CE F101','101'),('CE F102','101'),('CE F102','D333'),('CE F103','101'),('CE F104','102'),('CE F105','103'),('CE F106','103'),('CE F107','103'),('CS F101','104'),('CS F101','D111'),('CS F102','104'),('CS F103','105'),('CS F103','D222'),('CS F104','105');
/*!40000 ALTER TABLE `courseclass` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `instructor`
--

DROP TABLE IF EXISTS `instructor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instructor` (
  `instructId` varchar(10) NOT NULL,
  `name` varchar(20) DEFAULT NULL,
  `department` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`instructId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `instructor`
--

LOCK TABLES `instructor` WRITE;
/*!40000 ALTER TABLE `instructor` DISABLE KEYS */;
INSERT INTO `instructor` VALUES ('CE1','Sridhar Raju','Civil'),('CE2','Arkamitra kar','Civil'),('CE3','Srinvasa Raju','Civil'),('CE4','Raghuram','Civil'),('CE5','Chandu Parimi','Civil'),('CE6','A Vasan','Civil'),('CE7','Murari Verma','Civil'),('CS1','Barsha Mitra','CS'),('CS2','Apurva Das','CS'),('CS3','Abhijit Das','CS'),('CS4','Chitranjan Hota','CS');
/*!40000 ALTER TABLE `instructor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timeslot`
--

DROP TABLE IF EXISTS `timeslot`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timeslot` (
  `slotId` int NOT NULL AUTO_INCREMENT,
  `courseCode` varchar(10) DEFAULT NULL,
  `startTime` time DEFAULT NULL,
  `endTime` time DEFAULT NULL,
  PRIMARY KEY (`slotId`),
  KEY `courseCode` (`courseCode`),
  CONSTRAINT `timeslot_ibfk_1` FOREIGN KEY (`courseCode`) REFERENCES `course` (`courseCode`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timeslot`
--

LOCK TABLES `timeslot` WRITE;
/*!40000 ALTER TABLE `timeslot` DISABLE KEYS */;
INSERT INTO `timeslot` VALUES (1,'CE F101','09:00:00','09:50:00'),(2,'CE F102','10:00:00','10:50:00'),(3,'CE F102','14:00:00','15:50:00'),(4,'CE F103','11:00:00','11:50:00'),(5,'CE F104','14:00:00','14:50:00'),(6,'CE F105','14:00:00','14:50:00'),(7,'CE F106','15:00:00','15:50:00'),(8,'CE F107','09:00:00','09:50:00'),(9,'CS F101','09:00:00','09:50:00'),(10,'CS F101','14:00:00','15:50:00'),(11,'CS F102','08:00:00','08:50:00'),(12,'CS F103','08:00:00','08:50:00'),(13,'CS F103','16:00:00','17:50:00'),(14,'CS F104','09:00:00','09:50:00');
/*!40000 ALTER TABLE `timeslot` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timeslotday`
--

DROP TABLE IF EXISTS `timeslotday`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timeslotday` (
  `slotId` int DEFAULT NULL,
  `day` varchar(10) DEFAULT NULL,
  KEY `slotId` (`slotId`),
  CONSTRAINT `timeslotday_ibfk_1` FOREIGN KEY (`slotId`) REFERENCES `timeslot` (`slotId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timeslotday`
--

LOCK TABLES `timeslotday` WRITE;
/*!40000 ALTER TABLE `timeslotday` DISABLE KEYS */;
INSERT INTO `timeslotday` VALUES (1,'Monday'),(1,'Wednesday'),(1,'Friday'),(2,'Tuesday'),(2,'Thursday'),(3,'Thursday'),(4,'Monday'),(4,'Wednesday'),(4,'Friday'),(5,'Tuesday'),(5,'Thursday'),(6,'Monday'),(6,'Wednesday'),(6,'Friday'),(7,'Monday'),(7,'Wednesday'),(8,'Tuesday'),(8,'Thursday'),(9,'Monday'),(9,'Tuesday'),(9,'Wednesday'),(10,'Thursday'),(11,'Tuesday'),(11,'Thursday'),(12,'Monday'),(12,'Wednesday'),(12,'Friday'),(13,'Tuesday'),(14,'Tuesday'),(14,'Thursday');
/*!40000 ALTER TABLE `timeslotday` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `timetable`
--

DROP TABLE IF EXISTS `timetable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timetable` (
  `timetableId` int NOT NULL AUTO_INCREMENT,
  `userId` varchar(20) DEFAULT NULL,
  `courseCode` varchar(10) DEFAULT NULL,
  `instructId` varchar(10) DEFAULT NULL,
  `classId` varchar(10) DEFAULT NULL,
  `slotId` int DEFAULT NULL,
  PRIMARY KEY (`timetableId`),
  KEY `userId` (`userId`),
  KEY `courseCode` (`courseCode`),
  KEY `instructId` (`instructId`),
  KEY `classId` (`classId`),
  KEY `slotId` (`slotId`),
  CONSTRAINT `timetable_ibfk_1` FOREIGN KEY (`userId`) REFERENCES `user` (`id`),
  CONSTRAINT `timetable_ibfk_2` FOREIGN KEY (`courseCode`) REFERENCES `course` (`courseCode`),
  CONSTRAINT `timetable_ibfk_3` FOREIGN KEY (`instructId`) REFERENCES `instructor` (`instructId`),
  CONSTRAINT `timetable_ibfk_4` FOREIGN KEY (`classId`) REFERENCES `classroom` (`classId`),
  CONSTRAINT `timetable_ibfk_5` FOREIGN KEY (`slotId`) REFERENCES `timeslot` (`slotId`)
) ENGINE=InnoDB AUTO_INCREMENT=77 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `timetable`
--

LOCK TABLES `timetable` WRITE;
/*!40000 ALTER TABLE `timetable` DISABLE KEYS */;
INSERT INTO `timetable` VALUES (1,'2022HARS6478','CS F103','CS3','105',12),(2,'2022HARS6478','CS F103','CS3','105',12),(3,'2022HARS6478','CS F103','CS3','105',12),(4,'2022HARS6478','CS F103','CS3','D222',13),(5,'2022HARS6478','CE F104','CE2','102',5),(6,'2022HARS6478','CE F104','CE2','102',5),(7,'2022HARS6478','CE F105','CE4','103',6),(8,'2022HARS6478','CE F105','CE4','103',6),(9,'2022HARS6478','CE F105','CE4','103',6),(10,'2022HARS6478','CE F106','CE6','103',7),(11,'2022HARS6478','CE F106','CE6','103',7),(12,'2022HARS6478','CS F104','CS4','105',14),(13,'2022HARS6478','CS F104','CS4','105',14),(14,'2022HARS6478','CE F103','CE3','101',4),(15,'2022HARS6478','CE F103','CE3','101',4),(16,'2022HARS6478','CE F103','CE3','101',4),(17,'2022HARS6478','CE F101','CE5','101',1),(18,'2022HARS6478','CE F101','CE5','101',1),(19,'2022HARS6478','CE F101','CE5','101',1),(20,'2022HARS6478','CS F102','CS1','104',8),(21,'2022HARS6478','CE F107','CE7','103',3);
/*!40000 ALTER TABLE `timetable` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` varchar(20) NOT NULL,
  `fullName` varchar(20) NOT NULL,
  `email` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL,
  `role` varchar(10) NOT NULL DEFAULT 'student',
  `dept` varchar(20) NOT NULL DEFAULT 'CS',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`),
  CONSTRAINT `MINIMO` CHECK ((length(`password`) >= 6))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES ('2022ADMIN111','admin1','admin@gmail.com','123456','admin','head'),('2022HARS6478','Harshit Choubey','h@gmail.com','123456','student','Civil');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-04-20 16:26:40
