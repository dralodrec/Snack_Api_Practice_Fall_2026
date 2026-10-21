package com.movieapi.snack;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface snackRepository extends JpaRepository<snack, Long> {

    Iterable<snack> findSnackByPriceLessThanEqual(Double price);
}

// what interface should SnackRepository extend? CrudRepository or JpaRepository?
// snackRepository.findAll();       // Get all snacks
// snackRepository.findById(1L);    // Find by ID
// snackRepository.save(snack);     // Create or update
// snackRepository.deleteById(1L);  // Delete by ID
// snackRepository.count();         // Count records
// snackRepository.existsById(1L);  // Check existence
