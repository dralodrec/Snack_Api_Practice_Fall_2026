package com.movieapi.snack;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;


@Entity
    public class snack {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long snackId;
        private String name;
        private String type;
        private Double price;
        
        // Constructors
        public snack() {}

        public snack(String name, String type, Double price) {
            this.name = name;
            this.type = type;
            this.price = price;
        }

        // Getters and Setters
        public Long getSnackId() {
            return snackId;
        }

        public void setSnackId(Long snackId) {
            this.snackId = snackId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }
    }
