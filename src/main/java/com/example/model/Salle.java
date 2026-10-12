package com.example.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    private int capacite;

    @OneToMany(mappedBy = "salle",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<Reservation> reservations = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "salle_equipement",
            joinColumns = @JoinColumn(name = "salle_id"),
            inverseJoinColumns = @JoinColumn(name = "equipement_id")
    )
    private Set<Equipement> equipements = new HashSet<>();

    public Salle() {
    }

    public Salle(Long id, String nom, int capacite, Set<Reservation> reservations) {
        this.id = id;
        this.nom = nom;
        this.capacite = capacite;
        this.reservations = reservations;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public Set<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(Set<Reservation> reservations) {
        this.reservations = reservations;
    }

    public Set<Equipement> getEquipements() {
        return equipements;
    }

    public void setEquipements(Set<Equipement> equipements) {
        this.equipements = equipements;
    }

    public void addEquipement(Equipement equipement) {
        equipements.add(equipement);
        equipement.getSalles().add(this);
    }
    public void addReservation(Reservation reservation) {
        reservations.add(reservation);
        reservation.setSalle(this);
    }


    public boolean estDisponible(LocalDate debut, LocalDate fin) {

        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new IllegalArgumentException("Période invalide");
        }

        for (Reservation r : reservations) {

            if (r.getDateDebut().isBefore(fin)
                    && r.getDateFin().isAfter(debut)) {
                return false;
            }
        }

        return true;
    }
}