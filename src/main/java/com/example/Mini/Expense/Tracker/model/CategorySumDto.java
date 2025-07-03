package com.example.Mini.Expense.Tracker.model;

import lombok.Getter;

import java.math.BigDecimal;


@Getter
public class CategorySumDto {

    private String category;
    private BigDecimal sum;

    public CategorySumDto(String category, BigDecimal sum) {
        this.category = category;
        this.sum = sum;
    }
}
