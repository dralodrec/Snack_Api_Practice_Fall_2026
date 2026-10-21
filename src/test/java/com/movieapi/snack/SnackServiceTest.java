package com.movieapi.snack;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class snackServiceTest {

    @Mock
    private snackRepository snackRepository;

    @InjectMocks
    private snackService snackService;

    private snack testSnack;

    @BeforeEach
    void setUp() {
        testSnack = new snack("Popcorn", "Food", 4.99);
        testSnack.setSnackId(1L);
    }

    // TEST 1: Save a snack
    @Test
    void testSaveSnack() {
        String result = snackService.saveSnack(testSnack);

        verify(snackRepository).save(testSnack);
        assertEquals("Snack saved successfully", result);
    }

    // TEST 2: Save another snack
    @Test
    void testSaveAnotherSnack() {
        snack drink = new snack("Cola", "Drink", 2.99);

        String result = snackService.saveSnack(drink);

        verify(snackRepository).save(drink);
        assertNotNull(result);
    }

    // TEST 3: Find an existing snack
    @Test
    void testGetSnackByIdFound() {
        when(snackRepository.findById(1L))
                .thenReturn(Optional.of(testSnack));

        Optional<snack> result = snackService.getSnackById(1L);

        assertTrue(result.isPresent());
        assertEquals("Popcorn", result.get().getName());
    }

    // TEST 4: Find a missing snack
    @Test
    void testGetSnackByIdNotFound() {
        when(snackRepository.findById(99L))
                .thenReturn(Optional.empty());

        Optional<snack> result = snackService.getSnackById(99L);

        assertTrue(result.isEmpty());
    }

    // TEST 5: Retrieve all snacks
    @Test
    void testGetAllSnacks() {
        snack drink = new snack("Cola", "Drink", 2.99);

        when(snackRepository.findAll())
                .thenReturn(List.of(testSnack, drink));

        Iterable<snack> result = snackService.getAllSnacks();

        assertEquals(2, countSnacks(result));
    }

    // TEST 6: Retrieve an empty snack list
    @Test
    void testGetAllSnacksEmpty() {
        when(snackRepository.findAll())
                .thenReturn(List.of());

        Iterable<snack> result = snackService.getAllSnacks();

        assertEquals(0, countSnacks(result));
    }

    // TEST 7: Find snacks within a price limit
    @Test
    void testFindByPrice() {
        when(snackRepository.findSnackByPriceLessThanEqual(5.00))
                .thenReturn(List.of(testSnack));

        Iterable<snack> result = snackService.findByPrice(5.00);

        assertEquals(1, countSnacks(result));
    }

    // TEST 8: No snacks match the price limit
    @Test
    void testFindByPriceNoResults() {
        when(snackRepository.findSnackByPriceLessThanEqual(1.00))
                .thenReturn(List.of());

        Iterable<snack> result = snackService.findByPrice(1.00);

        assertEquals(0, countSnacks(result));
    }

    // TEST 9: Delete a snack
    @Test
    void testDeleteSnack() {

        // Snack exists
        when(snackRepository.existsById(1L)).thenReturn(true);

        snackService.deleteSnack(1L);

        verify(snackRepository).deleteById(1L);
    }

    // TEST 10: Update a snack
    @Test
    void testUpdateSnack() {
        snack updatedSnack = new snack(
                "Large Popcorn", "Food", 6.99
        );

        when(snackRepository.findById(1L))
                .thenReturn(Optional.of(testSnack));

        when(snackRepository.save(any(snack.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        snack result = snackService.updateSnack(1L, updatedSnack);

        assertEquals("Large Popcorn", result.getName());
        assertEquals(6.99, result.getPrice());
        assertEquals(1L, result.getSnackId());

        verify(snackRepository).save(testSnack);
    }


    // TEST 11: Delete a non-existent snack
    @Test
    void testDeleteSnackNotFound() {

        // Snack does not exist
        when(snackRepository.existsById(99L)).thenReturn(false);

        snackService.deleteSnack(99L);

        verify(snackRepository, never()).deleteById(99L);
    }

    // Helper method: Count snacks in an Iterable
    private int countSnacks(Iterable<snack> snacks) {
        int count = 0;
        for (snack item : snacks) {
            count++;
        }
        return count;
    }
}
