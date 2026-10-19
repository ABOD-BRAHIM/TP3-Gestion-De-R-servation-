package com.example;

import com.example.model.Salle;
import com.example.model.Utilisateur;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.Cache;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class CacheL2Test {

    private static EntityManagerFactory emf;

    @BeforeClass
    public static void setUp() {
        emf = Persistence.createEntityManagerFactory("gestion-reservations-pu");
    }

    @AfterClass
    public static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Test
    public void testSecondLevelCacheSalle() {
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();

        Salle salle = new Salle();
        salle.setNom("Salle L2 Cache Test");
        salle.setCapacite(25);
        em1.persist(salle);

        em1.getTransaction().commit();
        Long salleId = salle.getId();
        em1.close();

        // Premier find : charge l'entité et l'alimente dans le cache L2
        EntityManager em2 = emf.createEntityManager();
        Salle salleChargee1 = em2.find(Salle.class, salleId);
        assertNotNull(salleChargee1);
        em2.close();

        // Vérifier la présence dans le cache L2
        Cache cache = emf.getCache();
        assertTrue("La salle doit être présente dans le cache de second niveau (L2)", cache.contains(Salle.class, salleId));

        // Deuxième find dans une NOUVELLE session : servi directement par le cache L2 (sans requête SQL)
        EntityManager em3 = emf.createEntityManager();
        Salle salleChargee2 = em3.find(Salle.class, salleId);
        assertNotNull(salleChargee2);
        em3.close();
    }

    @Test
    public void testSecondLevelCacheUtilisateur() {
        EntityManager em1 = emf.createEntityManager();
        em1.getTransaction().begin();

        Utilisateur user = new Utilisateur();
        user.setEmail("cache_user_" + System.currentTimeMillis() + "@example.com");
        user.setNom("Utilisateur Cache L2");
        em1.persist(user);

        em1.getTransaction().commit();
        Long userId = user.getId();
        em1.close();

        // Premier find : charge et met en cache L2
        EntityManager em2 = emf.createEntityManager();
        Utilisateur userCharge1 = em2.find(Utilisateur.class, userId);
        assertNotNull(userCharge1);
        em2.close();

        // Vérifier la présence dans le cache L2
        Cache cache = emf.getCache();
        assertTrue("L'utilisateur doit être présent dans le cache de second niveau (L2)", cache.contains(Utilisateur.class, userId));

        // Deuxième find dans une NOUVELLE session : servi depuis le cache L2
        EntityManager em3 = emf.createEntityManager();
        Utilisateur userCharge2 = em3.find(Utilisateur.class, userId);
        assertNotNull(userCharge2);
        em3.close();
    }
}
