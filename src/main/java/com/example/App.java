
package com.example;

import com.example.model.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

public class App {

    public static void main(String[] args) {

        EntityManagerFactory emf =
                Persistence.createEntityManagerFactory(
                        "gestion-reservations-pu"
                );

        EntityManager em = null;

        try {
            em = emf.createEntityManager();

            em.getTransaction().begin();

            Utilisateur user = new Utilisateur();
            user.setEmail("alice" + System.currentTimeMillis() + "@example.com");
            user.setNom("Alice Dupont");

            Salle salle = new Salle();
            salle.setNom("Salle A");
            salle.setCapacite(30);

            Equipement projecteur = new Equipement();
            projecteur.setNom("Projecteur " + System.currentTimeMillis());

            salle.addEquipement(projecteur);

            em.persist(user);
            em.persist(salle);
            em.persist(projecteur);

            Reservation reservation = new Reservation();
            reservation.setDateReservation(LocalDate.now());
            reservation.setDateDebut(LocalDate.of(2026, 10, 15));
            reservation.setDateFin(LocalDate.of(2026, 10, 17));
            reservation.verifierDates();

            user.addReservation(reservation);
            salle.addReservation(reservation);

            em.persist(reservation);

            em.getTransaction().commit();

            Long userId = user.getId();
            Long salleId = salle.getId();
            Long reservationId = reservation.getId();

            List<Reservation> reservationsUtilisateur =
                    em.createQuery(
                                    "SELECT r FROM Reservation r WHERE r.utilisateur.id = :userId",
                                    Reservation.class
                            )
                            .setParameter("userId", userId)
                            .getResultList();

            System.out.println(
                    "Nombre de réservations de l'utilisateur : "
                            + reservationsUtilisateur.size()
            );

            List<Salle> sallesAvecProjecteur =
                    em.createQuery(
                                    "SELECT DISTINCT s FROM Salle s JOIN s.equipements e WHERE e.nom LIKE :nom",
                                    Salle.class
                            )
                            .setParameter("nom", "%Projecteur%")
                            .getResultList();

            System.out.println(
                    "Nombre de salles avec projecteur : "
                            + sallesAvecProjecteur.size()
            );

            for (Salle s : sallesAvecProjecteur) {
                System.out.println("Salle : " + s.getNom());
            }

            Salle sallePourVerifier = em.find(Salle.class, salleId);

            boolean disponible = sallePourVerifier.estDisponible(
                    LocalDate.of(2026, 10, 15),
                    LocalDate.of(2026, 10, 17)
            );

            System.out.println("Salle disponible : " + disponible);

            Utilisateur utilisateurTrouve =
                    em.find(Utilisateur.class, userId);

            Salle salleTrouvee =
                    em.find(Salle.class, salleId);

            System.out.println(
                    "Utilisateur trouvé : " + (utilisateurTrouve != null)
            );

            System.out.println(
                    "Salle trouvée : " + (salleTrouvee != null)
            );

            em.getTransaction().begin();

            Salle salleAModifier = em.find(Salle.class, salleId);
            salleAModifier.setNom("Salle B");

            em.getTransaction().commit();

            System.out.println(
                    "Nouveau nom : "
                            + em.find(Salle.class, salleId).getNom()
            );

            Reservation reservationTrouvee =
                    em.find(Reservation.class, reservationId);

            System.out.println(
                    "Utilisateur associé : "
                            + (reservationTrouvee.getUtilisateur() != null)
            );

            System.out.println(
                    "Salle associée : "
                            + (reservationTrouvee.getSalle() != null)
            );

            System.out.println(
                    "Nombre d'équipements : "
                            + salleTrouvee.getEquipements().size()
            );

            em.getTransaction().begin();

            Salle salleRollback = em.find(Salle.class, salleId);
            salleRollback.setNom("Nom temporaire");

            em.getTransaction().rollback();
            em.clear();

            System.out.println(
                    "Nom après rollback : "
                            + em.find(Salle.class, salleId).getNom()
            );

            em.getTransaction().begin();

            Reservation reservationASupprimer =
                    em.find(Reservation.class, reservationId);

            if (reservationASupprimer != null) {
                em.remove(reservationASupprimer);
            }

            em.getTransaction().commit();
            em.clear();

            System.out.println(
                    "Réservation supprimée : "
                            + (em.find(Reservation.class, reservationId) == null)
            );

            em.getTransaction().begin();

            Utilisateur userCascade = new Utilisateur();
            userCascade.setEmail(
                    "cascade" + System.currentTimeMillis() + "@example.com"
            );
            userCascade.setNom("Utilisateur Cascade");

            em.persist(userCascade);

            Reservation reservationCascade = new Reservation();
            reservationCascade.setDateReservation(LocalDate.now());
            reservationCascade.setDateDebut(LocalDate.of(2026, 11, 1));
            reservationCascade.setDateFin(LocalDate.of(2026, 11, 2));
            reservationCascade.verifierDates();

            userCascade.addReservation(reservationCascade);

            em.getTransaction().commit();

            Long userCascadeId = userCascade.getId();
            Long reservationCascadeId = reservationCascade.getId();

            em.clear();
            em.getTransaction().begin();

            Utilisateur userASupprimer =
                    em.find(Utilisateur.class, userCascadeId);

            em.remove(userASupprimer);

            em.getTransaction().commit();
            em.clear();

            System.out.println(
                    "Utilisateur supprimé : "
                            + (em.find(Utilisateur.class, userCascadeId) == null)
            );

            System.out.println(
                    "Réservation supprimée par cascade : "
                            + (em.find(
                            Reservation.class,
                            reservationCascadeId
                    ) == null)
            );

            System.out.println("Tous les tests sont terminés.");

        } catch (Exception e) {

            System.err.println(
                    "Erreur lors d'une opération JPA : " + e.getMessage()
            );

            if (em != null && em.isOpen()) {
                try {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                } catch (Exception erreurRollback) {
                    System.err.println(
                            "Erreur pendant le rollback : "
                                    + erreurRollback.getMessage()
                    );
                }
            }

            e.printStackTrace();

        } finally {

            if (em != null) {
                try {
                    if (em.isOpen()) {
                        em.close();
                    }
                } catch (Exception e) {
                    System.err.println(
                            "Erreur fermeture EntityManager : "
                                    + e.getMessage()
                    );
                }
            }

            try {
                if (emf.isOpen()) {
                    emf.close();
                }
            } catch (Exception e) {
                System.err.println(
                        "Erreur fermeture EntityManagerFactory : "
                                + e.getMessage()
                );
            }
        }
    }
}