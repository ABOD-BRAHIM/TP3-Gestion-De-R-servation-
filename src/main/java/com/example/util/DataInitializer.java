package com.example.util;

import com.example.model.Equipement;
import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.time.LocalDate;

public class DataInitializer {

    private final EntityManager em;

    public DataInitializer(EntityManager em) {
        this.em = em;
    }

    public void initData() {
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        // 1. Création des équipements
        Equipement videoprojecteur = new Equipement();
        videoprojecteur.setNom("Vidéoprojecteur 4K " + System.currentTimeMillis());
        em.persist(videoprojecteur);

        Equipement tableauBlanc = new Equipement();
        tableauBlanc.setNom("Tableau Blanc Interactif " + System.currentTimeMillis());
        em.persist(tableauBlanc);

        // 2. Création de plusieurs salles avec différents profils
        Salle salle1 = new Salle();
        salle1.setNom("Salle A - Turing");
        salle1.setCapacite(20);
        salle1.setDescription("Salle de formation équipée multimédia");
        salle1.addEquipement(videoprojecteur);
        em.persist(salle1);

        Salle salle2 = new Salle();
        salle2.setNom("Salle B - Lovelace");
        salle2.setCapacite(50);
        salle2.setDescription("Grand amphithéâtre pour séminaires");
        salle2.addEquipement(videoprojecteur);
        salle2.addEquipement(tableauBlanc);
        em.persist(salle2);

        Salle salle3 = new Salle();
        salle3.setNom("Salle C - Hopper");
        salle3.setCapacite(10);
        salle3.setDescription("Petite salle de réunion pour brainstorming");
        salle3.addEquipement(tableauBlanc);
        em.persist(salle3);

        // 3. Création de plusieurs utilisateurs
        Utilisateur user1 = new Utilisateur();
        user1.setNom("Alice Dupont");
        user1.setEmail("alice.dupont." + System.currentTimeMillis() + "@example.com");
        em.persist(user1);

        Utilisateur user2 = new Utilisateur();
        user2.setNom("Bob Martin");
        user2.setEmail("bob.martin." + System.currentTimeMillis() + "@example.com");
        em.persist(user2);

        // 4. Création de réservations réalistes avec dates dynamiques
        Reservation res1 = new Reservation();
        res1.setDateReservation(LocalDate.now());
        res1.setDateDebut(LocalDate.now().plusDays(1));
        res1.setDateFin(LocalDate.now().plusDays(3));
        res1.setUtilisateur(user1);
        res1.setSalle(salle1);
        user1.getReservations().add(res1);
        salle1.getReservations().add(res1);
        em.persist(res1);

        Reservation res2 = new Reservation();
        res2.setDateReservation(LocalDate.now());
        res2.setDateDebut(LocalDate.now().plusDays(5));
        res2.setDateFin(LocalDate.now().plusDays(7));
        res2.setUtilisateur(user2);
        res2.setSalle(salle2);
        user2.getReservations().add(res2);
        salle2.getReservations().add(res2);
        em.persist(res2);

        tx.commit();
    }
}
