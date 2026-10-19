package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import org.junit.jupiter.api.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.time.LocalDate;
import java.util.List;

public class ReservationTest {

    private static EntityManagerFactory emf;
    private EntityManager em;

    @BeforeAll
    public static void init() {
        emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
    }

    @BeforeEach
    public void setUp() {
        em = emf.createEntityManager();
    }

    @AfterEach
    public void tearDown() {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }

    @AfterAll
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    public void testCreateReservation() {
        em.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setNom("Test User");
        user.setEmail("test.user." + System.currentTimeMillis() + "@example.com");
        em.persist(user);

        Salle salle = new Salle();
        salle.setNom("Salle Test Creation");
        salle.setCapacite(15);
        salle.setDescription("Salle dédiée aux tests unitaires");
        em.persist(salle);

        Reservation reservation = new Reservation();
        reservation.setDateReservation(LocalDate.now());
        reservation.setDateDebut(LocalDate.now().plusDays(2));
        reservation.setDateFin(LocalDate.now().plusDays(4));
        reservation.setUtilisateur(user);
        reservation.setSalle(salle);
        user.getReservations().add(reservation);
        salle.getReservations().add(reservation);

        em.persist(reservation);
        em.getTransaction().commit();

        Assertions.assertNotNull(reservation.getId(), "L'ID de réservation ne doit pas être null après persist");
        Assertions.assertTrue(reservation.getId() > 0);
    }

    @Test
    public void testConflictReservationDetection() {
        em.getTransaction().begin();

        Salle salle = new Salle();
        salle.setNom("Salle Conflit Test");
        salle.setCapacite(10);
        em.persist(salle);

        Utilisateur user = new Utilisateur();
        user.setNom("Conflit User");
        user.setEmail("conflit.user." + System.currentTimeMillis() + "@example.com");
        em.persist(user);

        Reservation resInitiale = new Reservation();
        resInitiale.setDateReservation(LocalDate.now());
        resInitiale.setDateDebut(LocalDate.of(2026, 12, 10));
        resInitiale.setDateFin(LocalDate.of(2026, 12, 15));
        resInitiale.setUtilisateur(user);
        resInitiale.setSalle(salle);
        salle.addReservation(resInitiale);
        user.addReservation(resInitiale);
        em.persist(resInitiale);

        em.getTransaction().commit();

        // Vérifier disponibilité
        boolean dispoChevauchement = salle.estDisponible(
                LocalDate.of(2026, 12, 12),
                LocalDate.of(2026, 12, 14)
        );
        Assertions.assertFalse(dispoChevauchement, "La salle ne doit pas être disponible sur une période déjà réservée");

        boolean dispoLibre = salle.estDisponible(
                LocalDate.of(2026, 12, 20),
                LocalDate.of(2026, 12, 25)
        );
        Assertions.assertTrue(dispoLibre, "La salle doit être disponible sur un créneau libre");
    }

    @Test
    public void testDateValidationFailure() {
        Reservation reservation = new Reservation();
        reservation.setDateDebut(LocalDate.of(2026, 11, 20));
        reservation.setDateFin(LocalDate.of(2026, 11, 10)); // fin avant début !

        Assertions.assertThrows(IllegalArgumentException.class, reservation::verifierDates,
                "Une IllegalArgumentException doit être levée lorsque la date début est après la date fin");
    }

    @Test
    public void testDeleteReservation() {
        em.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setNom("Delete Test User");
        user.setEmail("delete." + System.currentTimeMillis() + "@example.com");
        em.persist(user);

        Salle salle = new Salle();
        salle.setNom("Salle Delete Test");
        salle.setCapacite(8);
        em.persist(salle);

        Reservation res = new Reservation();
        res.setDateReservation(LocalDate.now());
        res.setDateDebut(LocalDate.now().plusDays(10));
        res.setDateFin(LocalDate.now().plusDays(12));
        res.setUtilisateur(user);
        res.setSalle(salle);
        em.persist(res);

        em.getTransaction().commit();
        Long resId = res.getId();

        em.clear();

        em.getTransaction().begin();
        Reservation aSupprimer = em.find(Reservation.class, resId);
        Assertions.assertNotNull(aSupprimer);
        em.remove(aSupprimer);
        em.getTransaction().commit();

        em.clear();
        Reservation chargeeApresSuppr = em.find(Reservation.class, resId);
        Assertions.assertNull(chargeeApresSuppr, "La réservation doit être supprimée de la base");
    }

    @Test
    public void testJPQLQuery() {
        em.getTransaction().begin();

        Salle salleJPQL = new Salle();
        salleJPQL.setNom("Salle JPQL Test");
        salleJPQL.setCapacite(20);
        em.persist(salleJPQL);

        Utilisateur userJPQL = new Utilisateur();
        userJPQL.setNom("User JPQL");
        userJPQL.setEmail("user.jpql." + System.currentTimeMillis() + "@example.com");
        em.persist(userJPQL);

        Reservation r = new Reservation();
        r.setDateReservation(LocalDate.now());
        r.setDateDebut(LocalDate.now().plusDays(5));
        r.setDateFin(LocalDate.now().plusDays(7));
        r.setUtilisateur(userJPQL);
        r.setSalle(salleJPQL);
        em.persist(r);

        em.getTransaction().commit();

        com.example.dao.ReservationDao dao = new com.example.dao.ReservationDao(em);
        List<Reservation> results = dao.findBySalleAndDateDebutAfterJPQL("Salle JPQL Test", LocalDate.now());

        Assertions.assertFalse(results.isEmpty(), "La requête JPQL doit retourner au moins une réservation");
        Assertions.assertEquals("Salle JPQL Test", results.get(0).getSalle().getNom());
    }

    @Test
    public void testCriteriaAPIQuery() {
        em.getTransaction().begin();

        Salle salleCriteria = new Salle();
        salleCriteria.setNom("Salle Criteria Test");
        salleCriteria.setCapacite(30);
        em.persist(salleCriteria);

        String email = "criteria." + System.currentTimeMillis() + "@example.com";
        Utilisateur userCriteria = new Utilisateur();
        userCriteria.setNom("User Criteria");
        userCriteria.setEmail(email);
        em.persist(userCriteria);

        Reservation r = new Reservation();
        r.setDateReservation(LocalDate.now());
        r.setDateDebut(LocalDate.now().plusDays(3));
        r.setDateFin(LocalDate.now().plusDays(4));
        r.setUtilisateur(userCriteria);
        r.setSalle(salleCriteria);
        em.persist(r);

        em.getTransaction().commit();

        com.example.dao.ReservationDao dao = new com.example.dao.ReservationDao(em);
        List<Reservation> results = dao.findReservationsCriteria("Salle Criteria Test", email, LocalDate.now());

        Assertions.assertEquals(1, results.size(), "Criteria API doit trouver la réservation correspondante aux critères");
        Assertions.assertEquals(email, results.get(0).getUtilisateur().getEmail());
    }

    @Test
    public void testReservationServiceLoggingAndErrorHandling() {
        em.getTransaction().begin();

        Salle salle = new Salle();
        salle.setNom("Salle Service Test");
        salle.setCapacite(12);
        em.persist(salle);

        Utilisateur user = new Utilisateur();
        user.setNom("User Service");
        user.setEmail("user.service." + System.currentTimeMillis() + "@example.com");
        em.persist(user);

        em.getTransaction().commit();

        com.example.service.ReservationService service = new com.example.service.ReservationService(em);

        // 1. Création réussie via le service
        Reservation cree = service.createReservation(user.getId(), salle.getId(), LocalDate.now().plusDays(20), LocalDate.now().plusDays(22));
        Assertions.assertNotNull(cree.getId());

        // 2. Gestion d'erreur : réservation sur un créneau en conflit
        Assertions.assertThrows(IllegalStateException.class, () -> {
            service.createReservation(user.getId(), salle.getId(), LocalDate.now().plusDays(20), LocalDate.now().plusDays(21));
        }, "Le service doit lever une IllegalStateException en cas de chevauchement");

        // 3. Annulation via le service
        service.cancelReservation(cree.getId());
        em.clear();
        Assertions.assertNull(em.find(Reservation.class, cree.getId()));
    }
}
