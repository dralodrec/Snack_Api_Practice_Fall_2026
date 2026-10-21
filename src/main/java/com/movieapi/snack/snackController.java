package com.movieapi.snack;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.movieapi.snack.snack;

@RestController
@CrossOrigin
@RequestMapping("/snacks")
public class snackController {

    @Autowired
    private snackService snackService;

    // POST save new snack
    @PostMapping()
    public ResponseEntity<String> saveNewSnack(@RequestBody snack snack) {
        return ResponseEntity.ok(snackService.saveSnack(snack));
    }

    // GET find all snacks
    @GetMapping("/getAllSnacks")
    public ResponseEntity<Iterable<snack>> getAllSnacks() {
        return ResponseEntity.ok(snackService.getAllSnacks());
    }

    // GET find snack by id
    @GetMapping("/{snackId}")
    public ResponseEntity<snack> getSnackById(@PathVariable Long snackId) {
        return ResponseEntity.ok(snackService.getSnackById(snackId).orElseThrow(() -> new RuntimeException("Snack not found with id: " + snackId)));
    }

    // GET find snacks by price with a maximum price of 5.00
    @GetMapping("/searchByPrice/{price}")
    public ResponseEntity<Iterable<snack>> get(@PathVariable Double price) {
        return ResponseEntity.ok(snackService.findByPrice(price));
    }

    // DELETE remove snack by id
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSnack(@PathVariable Long id) {
        snackService.deleteSnack(id);
        return ResponseEntity.ok("Snack deleted successfully");
    }

    // PUT update snack by id and return the updated snack
    @PutMapping("/{id}")
    public ResponseEntity<snack> updateSnack(@PathVariable Long id, @RequestBody snack updatedSnack) {
        snack snack = snackService.updateSnack(id, updatedSnack);
        return ResponseEntity.ok(snack);
    }

}
