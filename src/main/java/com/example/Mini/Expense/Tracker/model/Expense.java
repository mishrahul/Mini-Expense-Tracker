package com.example.Mini.Expense.Tracker.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;



@Entity
@Data
@Table(name="expenses")
@AllArgsConstructor
@NoArgsConstructor
//@RequiredArgsConstructor
@Getter
@Setter
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false, scale = 2)
    @Min(1)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private ExpenseCategory category;

    @Column(nullable=false)
    private LocalDate date;

    public Expense(String description, BigDecimal amount, ExpenseCategory category, LocalDate date) {


        this.description=description;
        this.amount=amount;
        this.category=category;
        this.date=date;
    }


    //  public Expense(long id, String description, BigDecimal amount, Object o, ExpenseCategory category, LocalDate date) {
    //}
}
