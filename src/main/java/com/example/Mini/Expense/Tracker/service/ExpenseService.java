package com.example.Mini.Expense.Tracker.service;


import com.example.Mini.Expense.Tracker.model.CategorySumDto;
import com.example.Mini.Expense.Tracker.model.Expense;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import com.example.Mini.Expense.Tracker.model.ExpenseDto;
import com.example.Mini.Expense.Tracker.repository.ExpenseRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class ExpenseService {


    @Autowired
    private ExpenseRepository repository;

//    public ExpenseService(ExpenseRepository reposiotry) {
//        this.repository = reposiotry;
//    }

    public ExpenseDto save(@Valid ExpenseDto dto) {

        repository.save(new Expense(dto.description, dto.amount, dto.category, dto.date));

        return dto;
    }

    public ExpenseDto find(long id) throws NoResourceFoundException {

        Optional<Expense> expenseOptional = repository.findById(id);

        if(expenseOptional.isPresent()) return mapDto(expenseOptional.get());

        else throw new NoResourceFoundException(HttpMethod.GET, "Unable to find expense. " +
                "No expense record found matching the given id: "+ id);

                //(ExpenseDto) repository.findById(id).orElse(new Expense());
    }

    public ExpenseDto mapDto(Expense expense) {

        return new ExpenseDto(expense.getId(), expense.getDescription(), expense.getAmount(),
                            expense.getCategory(), expense.getDate());
    }


    public void delete(long id) throws NoResourceFoundException {
        Optional<Expense> expenseOptional = repository.findById(id);

        if(expenseOptional.isPresent()) {
            repository.deleteById(id);

        }

        else throw new NoResourceFoundException(HttpMethod.DELETE, "Unable to delete expense. " +
                "No expense record found matching the given id: "+ id);

    }

    public ExpenseDto updateExpense(ExpenseDto expenseDto, long id) throws NoResourceFoundException {
        Optional<Expense> expenseOptional =  repository.findById(id);

        if(expenseOptional.isPresent()) {
            Expense expenseToUpdate = expenseOptional.get();

            expenseToUpdate.setDescription(expenseDto.description);
            expenseToUpdate.setAmount(expenseDto.amount);
            expenseToUpdate.setCategory(expenseDto.category);
            expenseToUpdate.setDate(expenseDto.date);
            repository.save(expenseToUpdate);
            return expenseDto;
        }

        else throw new NoResourceFoundException(HttpMethod.PUT, "Unable to update expense. " +
                "No expense record found matching the given id: "+ id);


    }

    public List<ExpenseDto> filteredSearch(LocalDate from, LocalDate to, ExpenseCategory category) {

        List<Expense> filteredExpenses = repository.filteredSearch(from, to, category.name());


        List<ExpenseDto> filteredExpensesDto = new ArrayList<>();
        for(Expense expense : filteredExpenses) {
            filteredExpensesDto.add(mapDto(expense));
        }

        return filteredExpensesDto;
    }

    public List<CategorySumDto> categorySum(int year, int month) {
        //return repository.categorySum(year, month);
        List<Object[]> results = repository.categorySum(year, month);
        List<CategorySumDto> dtos = new ArrayList<>();
        for (Object[] row : results) {
            String category = (String) row[0];
            BigDecimal total = (BigDecimal) row[1];
            dtos.add(new CategorySumDto(category, total));
        }
        return dtos;
    }



}
