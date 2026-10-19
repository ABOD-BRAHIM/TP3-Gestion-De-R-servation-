package com.example;

import com.example.util.DataInitializer;
import com.example.util.MigrationExecutor;
import com.example.util.PerformanceReport;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("=== DÉMARRAGE DE L'APPLICATION GESTION RÉSERVATIONS ===");
        System.out.println("==================================================");

        String jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";
        String jdbcUser = "sa";
        String jdbcPassword = "";

        // Étape 6 : Exécuter la migration SQL
        try {
            System.out.println("\n--- [1] Exécution de la migration de base de données ---");
            MigrationExecutor migrationExecutor = new MigrationExecutor(jdbcUrl, jdbcUser, jdbcPassword);
            migrationExecutor.executeMigration("db/migration/v2_0_add_description.sql");
            System.out.println("Migration SQL exécutée avec succès.");
        } catch (Exception e) {
            System.err.println("Erreur lors de la migration SQL : " + e.getMessage());
            e.printStackTrace();
        }

        EntityManagerFactory emf = null;
        EntityManager em = null;

        try {
            // Création de l'EntityManagerFactory
            emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
            em = emf.createEntityManager();

            // Étape 3 : Initialiser le jeu de données réaliste
            System.out.println("\n--- [2] Initialisation du jeu de données réaliste ---");
            DataInitializer initializer = new DataInitializer(em);
            initializer.initData();
            System.out.println("Jeu de données inséré avec succès.");

            // Étape 9 : Démonstration requêtes JPQL & Criteria API
            System.out.println("\n--- [3] Optimisation des requêtes : JPQL & Criteria API ---");
            com.example.dao.ReservationDao dao = new com.example.dao.ReservationDao(em);

            System.out.println("-> Exécution requête JPQL paramétrée...");
            java.util.List<com.example.model.Reservation> reservationsJPQL =
                    dao.findBySalleAndDateDebutAfterJPQL("Salle A - Turing", java.time.LocalDate.now());
            for (com.example.model.Reservation r : reservationsJPQL) {
                System.out.println("   [JPQL] Réservation #" + r.getId() + " - Salle: " + r.getSalle().getNom()
                        + " - Du " + r.getDateDebut() + " au " + r.getDateFin());
            }

            System.out.println("-> Exécution requête dynamique Criteria API...");
            java.util.List<com.example.model.Reservation> reservationsCriteria =
                    dao.findReservationsCriteria("Salle B - Lovelace", null, java.time.LocalDate.now());
            for (com.example.model.Reservation r : reservationsCriteria) {
                System.out.println("   [Criteria] Réservation #" + r.getId() + " - Utilisateur: " + r.getUtilisateur().getEmail());
            }

            // Étape 11 : Démonstration du Service avec Journalisation & Gestion des erreurs
            System.out.println("\n--- [4] Service Métier : Journalisation et Gestion des Erreurs ---");
            com.example.service.ReservationService service = new com.example.service.ReservationService(em);

            // Recherche des identifiants existants
            com.example.model.Salle salle = em.createQuery("SELECT s FROM Salle s WHERE s.nom = :nom", com.example.model.Salle.class)
                    .setParameter("nom", "Salle C - Hopper")
                    .getSingleResult();
            com.example.model.Utilisateur user = em.createQuery("SELECT u FROM Utilisateur u", com.example.model.Utilisateur.class)
                    .setMaxResults(1)
                    .getSingleResult();

            com.example.model.Reservation newRes = service.createReservation(
                    user.getId(),
                    salle.getId(),
                    java.time.LocalDate.now().plusDays(15),
                    java.time.LocalDate.now().plusDays(18)
            );
            System.out.println("Réservation créée via Service avec ID : " + newRes.getId());

            // Tentative en conflit pour démontrer la gestion d'erreur et le log SEVERE
            try {
                System.out.println("Tentative de réservation en conflit temporel (doit être refusée)...");
                service.createReservation(
                        user.getId(),
                        salle.getId(),
                        java.time.LocalDate.now().plusDays(16),
                        java.time.LocalDate.now().plusDays(17)
                );
            } catch (Exception expected) {
                System.out.println("Exception métier interceptée avec succès : " + expected.getMessage());
            }

            // Étape 7 : Générer le rapport de performance
            System.out.println("\n--- [5] Diagnostic & Rapport de performance Hibernate ---");
            PerformanceReport report = new PerformanceReport(em);
            report.printReport();

            System.out.println("\n--- [6] Fin d'exécution de l'application ---");

        } catch (Exception e) {
            System.err.println("Erreur pendant l'exécution de l'application : " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
            if (emf != null && emf.isOpen()) {
                emf.close();
            }
        }
    }
}
