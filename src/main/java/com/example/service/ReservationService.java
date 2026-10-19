package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationService {

    private static final Logger LOGGER = Logger.getLogger(ReservationService.class.getName());
    private final EntityManager em;

    public ReservationService(EntityManager em) {
        this.em = em;
    }

    public Reservation createReservation(Long utilisateurId, Long salleId, LocalDate dateDebut, LocalDate dateFin) {
        LOGGER.info(() -> String.format("Tentative de création d'une réservation : utilisateurId=%d, salleId=%d, début=%s, fin=%s",
                utilisateurId, salleId, dateDebut, dateFin));

        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Utilisateur utilisateur = em.find(Utilisateur.class, utilisateurId);
            if (utilisateur == null) {
                throw new IllegalArgumentException("Utilisateur introuvable avec l'identifiant : " + utilisateurId);
            }

            Salle salle = em.find(Salle.class, salleId);
            if (salle == null) {
                throw new IllegalArgumentException("Salle introuvable avec l'identifiant : " + salleId);
            }

            // Vérification de la disponibilité de la salle
            if (!salle.estDisponible(dateDebut, dateFin)) {
                throw new IllegalStateException(String.format("La salle '%s' n'est pas disponible entre le %s et le %s",
                        salle.getNom(), dateDebut, dateFin));
            }

            Reservation reservation = new Reservation();
            reservation.setDateReservation(LocalDate.now());
            reservation.setDateDebut(dateDebut);
            reservation.setDateFin(dateFin);
            reservation.verifierDates();

            utilisateur.addReservation(reservation);
            salle.addReservation(reservation);

            em.persist(reservation);
            tx.commit();

            LOGGER.info(() -> String.format("Création de la réservation #%d réussie pour l'utilisateur : %s dans la salle : %s",
                    reservation.getId(), utilisateur.getEmail(), salle.getNom()));

            return reservation;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la réservation : " + e.getMessage(), e);
            if (tx.isActive()) {
                try {
                    tx.rollback();
                    LOGGER.warning("Transaction annulée (rollback) suite à une erreur.");
                } catch (Exception rbEx) {
                    LOGGER.log(Level.SEVERE, "Erreur pendant le rollback de transaction", rbEx);
                }
            }
            throw e;
        }
    }

    public void cancelReservation(Long reservationId) {
        LOGGER.info(() -> "Demande d'annulation de la réservation #" + reservationId);
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Réservation introuvable pour ID : " + reservationId);
            }

            // Retirer des collections parentes pour cohérence
            if (reservation.getUtilisateur() != null) {
                reservation.getUtilisateur().getReservations().remove(reservation);
            }
            if (reservation.getSalle() != null) {
                reservation.getSalle().getReservations().remove(reservation);
            }

            em.remove(reservation);
            tx.commit();
            LOGGER.info(() -> "Réservation #" + reservationId + " annulée et supprimée avec succès.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'annulation de la réservation #" + reservationId + " : " + e.getMessage(), e);
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        }
    }
}
