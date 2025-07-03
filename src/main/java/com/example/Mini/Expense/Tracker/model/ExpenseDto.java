package com.example.Mini.Expense.Tracker.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ExpenseDto {

    public long id;

    public String description;

    public BigDecimal amount;

    public ExpenseCategory category;
    public LocalDate date;


//    public ExpenseDto (long id, String desc, BigDecimal amount,
//                                 ExpenseCategory category, LocalDate date) {
//        this.id  = id;
//        this. description = desc;
//        this.amount = amount;
//        this.category = category;
//        this.date = date;
//    }

}
