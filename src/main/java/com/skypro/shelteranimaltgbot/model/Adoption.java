package com.skypro.shelteranimaltgbot.model;

import com.skypro.shelteranimaltgbot.model.Enum.ProbationPeriod;
import org.springframework.lang.NonNull;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

/**
 * класс Adoption предназначен для того, чтобы зафиксировать процесс усыновления питомца,
 * Волонтер создает новую запись в БД, дата записи является отправной точкой для мониторинга
 * процесса адаптации питомца
 */

@Entity
public class Adoption {

    /**
     * Идентификационный номер
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * усыновитель
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * питомец
     */
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "pet_id")
    private Pet pet;

    /**
     * дата усыновления
     */
    @NonNull
    private LocalDate date;

    /**
     * список отчетов по усыновлению
     */
    @OneToMany(mappedBy = "adoption", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Report> reports;

    /**
     * количество дней испытательного срока
     */
    private Integer trialPeriod;


    /***
     * Статус испытательного срока
     * */
    private ProbationPeriod probationPeriod;

    public Adoption() {
        this.probationPeriod = ProbationPeriod.PASSING;
        this.date = LocalDate.now();
    }


    public Adoption(User user, Pet pet, Integer trialPeriod) {
        this.user = user;
        this.pet = pet;
        this.date = LocalDate.now();
        this.trialPeriod = trialPeriod;
        this.probationPeriod = ProbationPeriod.PASSING;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    @NonNull
    public LocalDate getDate() {
        return date;
    }

    public Set<Report> getReports() {
        return reports;
    }

    public void setReports(Set<Report> reports) {
        this.reports = reports;
    }

    public Integer getTrialPeriod() {
        return trialPeriod;
    }

    public void setTrialPeriod(Integer trialPeriod) {
        this.trialPeriod = trialPeriod;
    }

    public ProbationPeriod getProbationPeriod() {
        return probationPeriod;
    }

    public void setProbationPeriod(ProbationPeriod probationPeriod) {
        this.probationPeriod = probationPeriod;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Adoption adoption = (Adoption) o;
        return Objects.equals(id, adoption.id) && Objects.equals(user, adoption.user) && Objects.equals(pet, adoption.pet) && date.equals(adoption.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, pet, date);
    }

    @Override
    public String toString() {
        return "Adoption{" +
                "id=" + id +
                ", date=" + date +
                ", countDayAdoption=" + trialPeriod +
                '}';
    }
}
