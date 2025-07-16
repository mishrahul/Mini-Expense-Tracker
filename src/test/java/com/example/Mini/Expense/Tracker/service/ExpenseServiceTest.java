package com.example.Mini.Expense.Tracker.service;


import com.example.Mini.Expense.Tracker.model.CategorySumDto;
import com.example.Mini.Expense.Tracker.model.Expense;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import com.example.Mini.Expense.Tracker.model.ExpenseDto;
import com.example.Mini.Expense.Tracker.repository.ExpenseRepository;
import jakarta.validation.constraints.Min;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {


    @Mock
    private ExpenseRepository repository;

    @InjectMocks
    private ExpenseService expenseService;

    private Expense expense;

    private ExpenseDto expenseDto;


    @BeforeEach
    void setup() {
        expense = new Expense(1L, "Pizza and coke",
                new BigDecimal(25) , ExpenseCategory.FOOD,
                LocalDate.of(2025, 01, 15));
        expenseDto = new ExpenseDto(1L, "Pizza and coke",
                new BigDecimal(25) , ExpenseCategory.FOOD,
                LocalDate.of(2025, 01, 15));

    }



    @Test
    void testCreateExpense() {
        when(repository.save(any(Expense.class))).thenReturn(expense);

        ExpenseDto savedDto = expenseService.save(expenseDto);

        assertNotNull(savedDto);
        assertEquals(expenseDto.getDescription(), savedDto.getDescription());
        assertEquals(expenseDto.getAmount(), savedDto.getAmount());
        assertEquals(expenseDto.getCategory().name(), savedDto.getCategory().name());
        assertEquals(expenseDto.getDate(), savedDto.getDate());

        verify(repository, times(1)).save(any(Expense.class));
    }

    @Test
    void testFindExpenseById_Found() throws NoResourceFoundException {
        when(repository.findById(1L)).thenReturn(Optional.ofNullable(expense));

        ExpenseDto dtoFound = expenseService.find(1L);

        assertNotNull(dtoFound);
        assertEquals(expense.getId(), dtoFound.getId());
        assertEquals(expense.getDescription(), dtoFound.getDescription());
        assertEquals(expense.getAmount(), dtoFound.getAmount());
        assertEquals(expense.getCategory().name(), dtoFound.getCategory().name());
        assertEquals(expense.getDate(), dtoFound.getDate());


        verify(repository, times(1)).findById(1L);
    }


    @Test
    void  testFindExpenseById_NotFound() throws NoResourceFoundException {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoResourceFoundException.class, () -> expenseService.find(2L));
    }

    @Test
    void testUpdateExpensesById_Found() throws NoResourceFoundException {

        Expense updatedExpense = new Expense(1L, "Updated: Pizza, pasta and coke",
                new BigDecimal(35.00) , ExpenseCategory.FOOD,
                LocalDate.of(2025, 01, 17));

        ExpenseDto updatedExpenseDto = new ExpenseDto(1L, "Updated: Pizza, pasta and coke",
                new BigDecimal(35.00) , ExpenseCategory.FOOD,
                LocalDate.of(2025, 01, 17));

        when(repository.findById(1L)).thenReturn(Optional.of(expense));
        when(repository.save(any(Expense.class))).thenReturn(updatedExpense);

        ExpenseDto resultDto = expenseService.updateExpense(updatedExpenseDto, 1L);

        assertNotNull(resultDto);
        assertEquals(updatedExpenseDto.getId(), resultDto.getId());
        assertEquals(updatedExpenseDto.getDescription(), resultDto.getDescription());
        assertEquals(updatedExpenseDto.getAmount(), resultDto.getAmount());
        assertEquals(updatedExpenseDto.getCategory(), resultDto.getCategory());
        assertEquals(updatedExpenseDto.getDate(), resultDto.getDate());

        verify(repository, times(1)).findById(1L);
        verify(repository, times(1)).save(any(Expense.class));
    }


    @Test
    void testUpdateExpenseById_NotFound() {
        ExpenseDto updatedDto = new ExpenseDto(2L, "dummy description",
                                                new BigDecimal(20), ExpenseCategory.FOOD,
                                                LocalDate.of(2025, 04, 03));

        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoResourceFoundException.class, () -> expenseService.updateExpense(updatedDto, 2L));
    }


    @Test
    void testDeleteExpenseById_Found() {
        when(repository.findById(1L)).thenReturn(Optional.of(expense));
        doNothing().when(repository).deleteById(1L);

        assertDoesNotThrow(()-> expenseService.delete(1L));

        verify(repository, times(1)).deleteById(1l);

    }

    @Test
    void testDeleteExpenseById_NotFound() {
        when(repository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoResourceFoundException.class, () -> expenseService.delete(2L));
    }

    @Test
    void getFilteredExpenses() {
        LocalDate from = LocalDate.of(2025, 01, 01);
        LocalDate to = LocalDate.of(2025, 01, 31);
        ExpenseCategory category = ExpenseCategory.FOOD;

        List<Expense> expenseList = Arrays.asList(expense, new Expense(2L, "Paid University fees",
                                                    new BigDecimal(800), ExpenseCategory.ACADEMIC,
                                                    LocalDate.of(2025, 01, 23)));

        when(repository.filteredSearch(from, to, category.name())).thenReturn(expenseList);

        List<ExpenseDto> resultList = expenseService.filteredSearch(from, to, category);

        assertNotNull(resultList);
        assertEquals(2, resultList.size());
        assertEquals(ExpenseCategory.FOOD, resultList.get(0).getCategory());
        assertEquals(1L, resultList.get(0).getId());
        assertEquals("Pizza and coke", resultList.get(0).getDescription());
        assertEquals(new BigDecimal(25), resultList.get(0).getAmount());
        assertEquals(LocalDate.of(2025, 01, 15), resultList.get(0).getDate());

        verify(repository, times(1)).filteredSearch(from, to, category.name());
    }


    @Test
    void testCategorySum() {
        int year = 2024;
        int month = 2;

        CategorySumDto sumDto1 = new CategorySumDto("FOOD", BigDecimal.valueOf(35.80));
        CategorySumDto sumDto2 = new CategorySumDto("GROCERY", BigDecimal.valueOf(52.75));
        CategorySumDto sumDto3 = new CategorySumDto("MISC", BigDecimal.valueOf(190.50));

        List<CategorySumDto> expectedList= Arrays.asList(sumDto1, sumDto2, sumDto3);

        when(repository.categorySum(year, month)).thenReturn(Arrays.asList(
                new Object[]{"FOOD", BigDecimal.valueOf(35.80)},
                new  Object[]{"GROCERY", BigDecimal.valueOf(52.75)},
                new Object[]{"MISC", BigDecimal.valueOf(190.50)}
        ));

        List<CategorySumDto> actual = expenseService.categorySum(year, month);

        assertNotNull(actual);
        assertEquals(expectedList.size(), actual.size());
        assertEquals(expectedList.get(0).getCategory(), actual.get(0).getCategory());
        assertEquals(expectedList.get(0).getSum(), actual.get(0).getSum());

        assertEquals(expectedList.get(1).getCategory(), actual.get(1).getCategory());
        assertEquals(expectedList.get(1).getSum(), actual.get(1).getSum());

        assertEquals(expectedList.get(2).getCategory(), actual.get(2).getCategory());
        assertEquals(expectedList.get(2).getSum(), actual.get(2).getSum());

    }


}
