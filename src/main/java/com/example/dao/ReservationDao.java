package com.example.dao;

import com.example.model.Reservation;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReservationDao {

    private static final Logger LOGGER = Logger.getLogger(ReservationDao.class.getName());
    private final EntityManager em;

    public ReservationDao(EntityManager em) {
        this.em = em;
    }

    /**
     * Recherche avec JPQL paramétrée : réservations d'une salle à partir d'une date donnée.
     */
    public List<Reservation> findBySalleAndDateDebutAfterJPQL(String nomSalle, LocalDate date) {
        LOGGER.info(() -> String.format("Exécution JPQL : recherche réservations pour salle='%s' après %s", nomSalle, date));
        try {
            String jpql = "SELECT r FROM Reservation r JOIN FETCH r.salle s JOIN FETCH r.utilisateur u " +
                          "WHERE s.nom = :nomSalle AND r.dateDebut > :date ORDER BY r.dateDebut ASC";

            List<Reservation> results = em.createQuery(jpql, Reservation.class)
                    .setParameter("nomSalle", nomSalle)
                    .setParameter("date", date)
                    .getResultList();

            LOGGER.info(() -> String.format("JPQL : %d réservation(s) trouvée(s).", results.size()));
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'exécution de la requête JPQL", e);
            throw e;
        }
    }

    /**
     * Recherche dynamique avec Criteria API avec critères optionnels.
     */
    public List<Reservation> findReservationsCriteria(String nomSalle, String emailUtilisateur, LocalDate dateApres) {
        LOGGER.info(() -> String.format("Exécution Criteria API : filtres [salle='%s', email='%s', dateApres=%s]",
                nomSalle, emailUtilisateur, dateApres));
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Reservation> cq = cb.createQuery(Reservation.class);
            Root<Reservation> root = cq.from(Reservation.class);

            List<Predicate> predicates = new ArrayList<>();

            if (nomSalle != null && !nomSalle.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("salle").get("nom"), nomSalle));
            }

            if (emailUtilisateur != null && !emailUtilisateur.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("utilisateur").get("email"), emailUtilisateur));
            }

            if (dateApres != null) {
                predicates.add(cb.greaterThan(root.get("dateDebut"), dateApres));
            }

            cq.select(root).where(cb.and(predicates.toArray(new Predicate[0])));
            cq.orderBy(cb.asc(root.get("dateDebut")));

            List<Reservation> results = em.createQuery(cq).getResultList();
            LOGGER.info(() -> String.format("Criteria API : %d résultat(s) retourné(s).", results.size()));
            return results;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'exécution de la requête Criteria API", e);
            throw e;
        }
    }
}
