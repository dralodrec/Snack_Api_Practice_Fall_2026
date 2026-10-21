package com.movieapi.snack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.movieapi.movie.Movie;
import com.movieapi.snack.snack;

import java.util.Optional;

@Service
public class snackService {
    
    @Autowired
    private snackRepository snackRepository;

    // POST save new snack
    public String saveSnack(snack snack) {
        snackRepository.save(snack);
        return "Snack saved successfully";
    }

    // GET find snack by id
    public Optional<snack> getSnackById(Long snackId) {
        return snackRepository.findById(snackId);
    }

    // GET find all snacks
    public Iterable<snack> getAllSnacks() {
    return snackRepository.findAll();
    }

    // DELETE remove snack by id
    public void deleteSnack(Long id) {
       if (snackRepository.existsById(id)) {
        snackRepository.deleteById(id);
        }
    }
   
    // GET find snacks by price with a maximum price of 5.00
    public Iterable<snack> findByPrice(Double price) {
        return snackRepository.findSnackByPriceLessThanEqual(price);
    }

    // PUT update snack by id and return the updated snack
    public snack updateSnack(Long id, snack updatedSnack) {
        snack existingSnack = snackRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Snack not found: " + id));

        existingSnack.setName(updatedSnack.getName());
        existingSnack.setType(updatedSnack.getType());
        existingSnack.setPrice(updatedSnack.getPrice());

        return snackRepository.save(existingSnack);
    }
}
