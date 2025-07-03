package com.example.Mini.Expense.Tracker.controller;


import com.example.Mini.Expense.Tracker.model.CategorySumDto;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import com.example.Mini.Expense.Tracker.model.ExpenseDto;
import com.example.Mini.Expense.Tracker.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ExpenseController.class)
public class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    private ObjectMapper objectMapper;

    private ExpenseDto expenseDto;



    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        expenseDto = new ExpenseDto(1L, "Travelling",
                                    new BigDecimal(250.75), ExpenseCategory.TRAVEL,
                                    LocalDate.of(2024, 02, 13));

    }



    @Test
    void testCreateExpenseTest() throws Exception {
        when(expenseService.save(any(ExpenseDto.class))).thenReturn(expenseDto);

        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Travelling"))
                .andExpect(jsonPath("$.amount").value(250.75));

        verify(expenseService, times(1)).save(any(ExpenseDto.class));
    }

    @Test
    void testFindExpensesById_Found() throws Exception {
        when(expenseService.find(1L)).thenReturn(expenseDto);

        mockMvc.perform(get("/expenses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Travelling"));

        verify(expenseService, times(1)).find(1L);
    }


    @Test
    void testFindExpenseById_NotFound() throws Exception {
        when(expenseService.find(2L)).thenThrow(new NoResourceFoundException(HttpMethod.GET, null));

        mockMvc.perform(get("/expenses/{id}", 2L))
                .andExpect(status().isNotFound());

        verify(expenseService, times(1)).find(2L);
    }

    @Test
    void textUpdateExpenseById_Found() throws Exception {
        ExpenseDto dtoUpdate = new ExpenseDto(1L, "Visiting", new BigDecimal(60.35),
                                        ExpenseCategory.TRAVEL,
                                        LocalDate.of(2024, 02, 14));

        when(expenseService.updateExpense(any(ExpenseDto.class), eq(1L))).thenReturn(dtoUpdate);


        mockMvc.perform(put("/expenses/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoUpdate)))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense entry updated"));

        verify(expenseService, times(1)).updateExpense(any(ExpenseDto.class), eq(1L));
    }

    @Test
    void testUpdateExpenseById_NotFound() throws Exception {
        ExpenseDto dtoUpdate = new ExpenseDto(2L, "Vegetables shopping",
                                            new BigDecimal(20.85), ExpenseCategory.GROCERY,
                                            LocalDate.of(2024, 11, 23));

        when(expenseService.updateExpense(any(ExpenseDto.class), eq(2L))).thenThrow(new NoResourceFoundException(HttpMethod.PUT, null));


        mockMvc.perform(put("/expenses/{oid}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoUpdate)))
                .andExpect(status().isNotFound());

        verify(expenseService, times(1)).updateExpense(any(ExpenseDto.class), eq(2L));

    }


    @Test
    void testDeleteExpenseById_Found() throws Exception {
        doNothing().when(expenseService).delete(1l);

        mockMvc.perform(delete("/expenses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense entry deleted"));


        verify(expenseService, times(1)).delete(1L);
    }

    @Test
    void testDeleteExpenseById_NotFound() throws Exception {
        doThrow(new NoResourceFoundException(HttpMethod.DELETE, null)).when(expenseService).delete(2L);

        mockMvc.perform(delete("/expenses/{id}", 2L))
                .andExpect(status().isNotFound());


        verify(expenseService, times(1)).delete(2L);
    }


    @Test
    void testGetFilteredExpenses() throws Exception {
        LocalDate from =  LocalDate.of(2024, 03, 01);
        LocalDate to = LocalDate.of(2024, 03, 31);
        ExpenseCategory category = com.example.Mini.Expense.Tracker.model.ExpenseCategory.MISC;

        List<ExpenseDto> list = Arrays.asList(expenseDto, new ExpenseDto(2L,
                                    "Electronic equipments", new BigDecimal(125.99),
                                            ExpenseCategory.MISC,
                                            LocalDate.of(2024, 03, 17)));

        when(expenseService.filteredSearch(from, to, category)).thenReturn(list);


        mockMvc.perform(get("/expenses")
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("category", category.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Travelling"))
                .andExpect(jsonPath("$[1].description").value("Electronic equipments"))
                .andExpect(jsonPath("$[0].amount").value(250.75))
                .andExpect(jsonPath("$[1].amount").value(125.99));


        verify(expenseService, times(1)).filteredSearch(from, to, category);
    }

    @Test
    void testGetCaregorySum () throws Exception {
        int year = 2024;
        int month = 2;


        CategorySumDto sumDto1 = new CategorySumDto("FOOD", BigDecimal.valueOf(35.80));
        CategorySumDto sumDto2 = new CategorySumDto("GROCERY", BigDecimal.valueOf(52.75));
        CategorySumDto sumDto3 = new CategorySumDto("MISC", BigDecimal.valueOf(190.50));

        //CategorySumDto sumDto4 = new CategorySumDto("FOOD", BigDecimal.valueOf(25));
        //CategorySumDto sumDto5 = new CategorySumDto("GROCERY", BigDecimal.valueOf(4));


        List<CategorySumDto> categorySumList = Arrays.asList(sumDto1, sumDto2, sumDto3);

        when(expenseService.categorySum(year, month)).thenReturn(categorySumList);


        mockMvc.perform(get("/expenses/summary")
                            .param("year", String.valueOf(year))
                            .param("month", String.valueOf(month))
                            .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].sum").value(35.80))
                .andExpect(jsonPath("$[1].category").value("GROCERY"))
                .andExpect(jsonPath("$[1].sum").value(52.75))
                .andExpect(jsonPath("$[2].category").value("MISC"))
                .andExpect(jsonPath("$[2].sum").value(190.50));

    }

}

