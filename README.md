## TP3 : Gestion De Réservation 

1. Vue d'ensemble du projet dans l'IDE avec la structure des packages, le fichier de configuration de persistance et la classe principale d'exécution.
![1.png](screenshots/1.png)
2. Configuration du fichier persistence.xml définissant l'unité de persistance, la connexion à la base de données et les paramètres Hibernate.![15Persistenc.png](screenshots/15Persistenc.png)
3. Déclaration des bibliothèques nécessaires au fonctionnement du projet, incluant JPA (API Hibernate) et le pilote de la base de données H2.![16Pom.png](screenshots/16Pom.png)
4. Définition de l'entité Equipement avec ses attributs et son association Many-to-Many avec les salles.![2Equipement.png](screenshots/2Equipement.png)
5. Structure de l'entité Reservation reliant un utilisateur et une salle via des relations Many-to-One et Implémentation des règles de gestion et de validation des dates de début et de fin de réservation.
![3Reservation.png](screenshots/3Reservation.png)![4Réservation.png](screenshots/4R%C3%A9servation.png)
6. Définition de la classe Salle avec ses propriétés, la gestion des réservations en cascade et la liaison avec les équipements.
![5Salle.png](screenshots/5Salle.png)![6salle.png](screenshots/6salle.png)
7. Définition de la classe Utilisateur avec ses contraintes d'unicité sur l'email et ses informations personnelles.
![7utilisateur.png](screenshots/7utilisateur.png)![8utilisateur.png](screenshots/8utilisateur.png)
8. Initialisation d'un utilisateur, d'une salle et d'un projecteur, puis persistance des objets via l'EntityManager.![9App.png](screenshots/9App.png)
9. Exécution des requêtes JPQL pour récupérer les réservations d'un utilisateur et rechercher les salles équipées d'un projecteur.![10App.png](screenshots/10App.png)
10. Appel de la méthode métier de vérification des chevauchements de dates pour une salle, recherche d'entités par ID et modification transactionnelle du nom d'une salle.![11App.png](screenshots/11App.png)
11. Test de la suppression en cascade (CascadeType.ALL) permettant de vérifier qu'en supprimant un utilisateur, ses réservations associées sont automatiquement nettoyées.![12App.png](screenshots/12App.png)
12. Affichage du message de confirmation de la réussite de tous les tests, gestion propre des exceptions JPA/transactions, et fermeture des gestionnaires de persistance.![13App.png](screenshots/13App.png)![14App.png](screenshots/14App.png)
13. Traçage de la création des tables principales (Equipement, Reservation, Salle, salle_equipement, et Utilisateur) par l'ORM au démarrage de l'application.![17Exec.png](screenshots/17Exec.png)
14. Application des clés étrangères, des contraintes d'intégrité référentielle, suivies des premières requêtes SQL d'insertion (INSERT) pour les entités métier.![18exec.png](screenshots/18exec.png)