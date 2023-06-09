package com.skypro.shelteranimaltgbot.model;


import com.skypro.shelteranimaltgbot.model.enums.StatusPet;

import javax.persistence.*;
import java.util.Objects;

/**
 * Класс Pet, представляет сущность домашнего питомца
 */
@Entity
@Table(name = "pet")
public class Pet {

    /**
     * Идентификационный номер (id) домашнего питомца
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Вид домашнего питомца
     */
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_pet_id")
    private TypePet typePet;

    /**
     * Имя домашнего питомца
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Возраст домашнего питомца
     */
    @Column(name = "age", nullable = false)
    private Integer age;

    /**
     * Рост домашнего питомца
     */
    @Column(name = "height")
    private Integer height;

    /**
     * Описание домашнего питомца
     */
    @Column(name = "description")
    private String description;


    /**
     * характер домашнего питомца
     */
    @Column(name = "personality")
    private String personality;


    /**
     * Статус домашнего питомца
     *
     * @see StatusPet
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusPet statusPet;

    /**
     * Фото питомца
     */
    @Column(name = "photo")
    private String filePath;

    /**
     * приют
     */
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "shelter_id")
    private Shelter shelter;


    /**
     * Конструктор для создания объекта домашний питомец
     *
     * @param name      имя домашнего питомца
     * @param age       возраст домашнего питомца
     * @param typePet   вид домашнего питомца
     * @param statusPet статус домашнего питомца
     */
    public Pet(String name, Integer age, TypePet typePet, StatusPet statusPet) {
        this.name = name;
        this.age = age;
        this.typePet = typePet;
        this.statusPet = statusPet;
    }

    /**
     * Конструктор для создания объекта домашний питомец, без параметров
     */
    public Pet() {
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public Shelter getShelter() {
        return shelter;
    }

    public void setShelter(Shelter shelter) {
        this.shelter = shelter;
    }

    public TypePet getTypePet() {
        return typePet;
    }

    public void setTypePet(TypePet typePet) {
        this.typePet = typePet;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public StatusPet getStatusPet() {
        return statusPet;
    }

    public void setStatusPet(StatusPet statusPet) {
        this.statusPet = statusPet;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(id, pet.id) && Objects.equals(typePet, pet.typePet) && Objects.equals(name, pet.name) && Objects.equals(age, pet.age) && statusPet == pet.statusPet && Objects.equals(filePath, pet.filePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, typePet, name, age, statusPet, filePath);
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                //  ", typePet=" + typePet +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", statusPet=" + statusPet +
                ", filePath='" + filePath + '\'' +
                // ", shelter=" + shelter +
                '}';
    }
}
