package com.example.Mini.Expense.Tracker.controller;


import com.example.Mini.Expense.Tracker.model.CategorySumDto;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import com.example.Mini.Expense.Tracker.model.ExpenseDto;
import com.example.Mini.Expense.Tracker.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.List;

@RestController

@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;


//    public ExpenseController(ExpenseService expenseService) {
//        this.expenseService = expenseService;
//    }



    @PostMapping
    public ResponseEntity<ExpenseDto> createExpense(@Valid @RequestBody ExpenseDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(expenseService.save(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDto> getExpense(@PathVariable long id) {

        ExpenseDto dto;

        try {
            dto = expenseService.find(id);;
        } catch (NoResourceFoundException e) {

            return new ResponseEntity<>(new ExpenseDto(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(dto, HttpStatus.OK);
    }




    @PutMapping("/{id}")
    public ResponseEntity<String> updateExpense(@RequestBody ExpenseDto expenseDto,
                                                    @PathVariable long id) {
        ExpenseDto dto;
        try {
             dto = expenseService.updateExpense(expenseDto, id);
        } catch (NoResourceFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Expense entry updated", HttpStatus.OK);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteById(@PathVariable long id) {
        //<ExpenseDto> dto = Optional.ofNullable(expenseService.find(id));

        try {
            expenseService.delete(id);
        }
        catch (NoResourceFoundException e) {
            return new ResponseEntity<>(e.getMessage(),  HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Expense entry deleted", HttpStatus.OK);
    }
//
//
    @GetMapping
    public ResponseEntity<List<ExpenseDto>> filteredExpenses(@RequestParam @DateTimeFormat LocalDate from,
                                                             @RequestParam @DateTimeFormat LocalDate to,
                                                             @RequestParam ExpenseCategory category) {
        List<ExpenseDto> filteredExpenses = expenseService.filteredSearch(from, to, category);


        return new ResponseEntity<>(filteredExpenses, HttpStatus.OK);
    }

    @GetMapping("/summary")
    public ResponseEntity<List<CategorySumDto>> getCategorySum(@RequestParam int year, @RequestParam int month) {
        return ResponseEntity.ok(expenseService.categorySum(year, month));
    }






}
