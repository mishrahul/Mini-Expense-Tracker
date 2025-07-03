package com.example.Mini.Expense.Tracker.repository;

import com.example.Mini.Expense.Tracker.model.CategorySumDto;
import com.example.Mini.Expense.Tracker.model.Expense;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    @Query(value="SELECT * FROM Expenses WHERE category = :categoryName"+
                    " AND date BETWEEN :from AND :to",
                    nativeQuery = true
    )
    List<Expense> filteredSearch(@Param("from") LocalDate from,
                                          @Param("to") LocalDate to,
                                          @Param("categoryName") String categoryName);



    @Query(value = "SELECT category, SUM(amount) as total " +
            "FROM expenses " +
            "WHERE YEAR(date) = :year AND MONTH(date) = :month " +
            "GROUP BY category", nativeQuery = true
    )
    List<Object[]> categorySum(@Param("year") int year, @Param("month") int month);


}
