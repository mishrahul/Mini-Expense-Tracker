package com.example.Mini.Expense.Tracker.integrationTesting;

import com.example.Mini.Expense.Tracker.model.Expense;
import com.example.Mini.Expense.Tracker.model.ExpenseCategory;
import com.example.Mini.Expense.Tracker.model.ExpenseDto;
import com.example.Mini.Expense.Tracker.repository.ExpenseRepository;
import com.example.Mini.Expense.Tracker.service.ExpenseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import groovy.transform.AutoImplement;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public class IntegrationTest {

    @LocalServerPort
    private Integer port;


    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");


    @Autowired
    private ExpenseRepository repo;


    private ExpenseDto expenseDto;

    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;


    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    static void start() {
        postgres.start();
    }

    @AfterAll
    static void stop() {
        postgres.stop();
    }


    @BeforeEach
     void setUp() {
        RestAssured.baseURI = "http://localhost:" + port;
        repo.deleteAllInBatch();
        jdbcTemplate.execute("TRUNCATE TABLE expenses RESTART IDENTITY");

        expenseDto = new ExpenseDto(1L, "Travelling",
                new BigDecimal(250.75), ExpenseCategory.TRAVEL,
                LocalDate.of(2024, 02, 13));

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void testCreateExpense() throws Exception {


        mockMvc.perform(post("/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expenseDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Travelling"))
                .andExpect(jsonPath("$.amount").value(250.75));

        List<Expense> list =  repo.findAll();
        Expense expense = list.get(0);
        assertEquals("Travelling", expense.getDescription());
        assertEquals(1L, expense.getId());
        assertEquals(250.75, expense.getAmount().doubleValue());
        assertEquals("TRAVEL", expense.getCategory().name());

    }

    @Test
    void testFindExpenseById_Exists() throws Exception {
        repo.save(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()));
        mockMvc.perform(MockMvcRequestBuilders.get("/expenses/{id}", 1L))
                        //.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Travelling"))
                .andExpect(jsonPath("$.amount").value(250.75));

        Optional<Expense> expenseOptl = repo.findById(1L);
        Expense expense = expenseOptl.orElse(null);

        assertTrue(expenseOptl.isPresent());

        assertEquals("Travelling", expense.getDescription());
        assertEquals(1L, expense.getId());
        assertEquals(250.75, expense.getAmount().doubleValue());
        assertEquals("TRAVEL", expense.getCategory().name());
    }

    @Test
    void testFindExpensesById_NotExists() throws Exception {
        mockMvc.perform(get("/expenses/{id}", 404L))
                .andExpect(status().isNotFound());

        Optional<Expense> expenseOptl = repo.findById(404L);
        assertFalse(expenseOptl.isPresent());
    }

    @Test
    void testUpdateExpenseById_Exists() throws Exception {
        repo.save(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()));

        ExpenseDto updatedDto = new ExpenseDto(expenseDto.getId(), "Updated description",
                new BigDecimal(270.75), ExpenseCategory.FOOD, LocalDate.of(2024, 03, 12));

        mockMvc.perform(put("/expenses/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense entry updated"));

        Optional<Expense> expenseOptl = repo.findById(expenseDto.getId());
        Expense updatedExpense = expenseOptl.orElse(null);
        assertTrue(expenseOptl.isPresent());
        assertNotNull(updatedExpense);

        assertEquals(updatedDto.getId(), updatedExpense.getId());
        assertEquals(updatedDto.getDescription(), updatedExpense.getDescription());
        assertEquals(updatedDto.getAmount(), updatedExpense.getAmount());
        assertEquals(updatedDto.getCategory().name(), updatedExpense.getCategory().name());
        assertEquals(updatedDto.getDate().toString(), updatedExpense.getDate().toString());

    }

    @Test
    void testUpdateExpenseById_NotExists() throws Exception {

        ExpenseDto updatedDto = new ExpenseDto(404L, "Updated description",
                new BigDecimal(270.75), ExpenseCategory.FOOD, LocalDate.of(2024, 03, 12));

        mockMvc.perform(put("/expenses/{id}", 404L).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isNotFound());

        Optional<Expense> expenseOptl = repo.findById(404L);
        assertFalse(expenseOptl.isPresent());
    }


    @Test
    void testDeleteExpenseById_Exists() throws Exception {
        repo.save(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()));

        Optional<Expense> expenseOptl1= repo.findById(1L);
        assertTrue(expenseOptl1.isPresent());

        assertNotNull(expenseOptl1.orElse(null));

        mockMvc.perform(delete("/expenses/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().string("Expense entry deleted"));

        Optional<Expense> expenseOptl2= repo.findById(1L);

        assertNull(expenseOptl2.orElse(null));
    }

    @Test
    void testDeleteExpenseById_NotExists() throws Exception{

        mockMvc.perform(delete("/expenses/{id}", 404L))
                .andExpect(status().isNotFound());

        Optional<Expense> expenseOptl = repo.findById(404L);
        assertFalse(expenseOptl.isPresent());
        assertNull(expenseOptl.orElse(null));

    }


    @Test
    void testFilteredExpenses() throws Exception {
        LocalDate from =  LocalDate.of(2024, 03, 01);
        LocalDate to = LocalDate.of(2024, 03, 31);
        ExpenseCategory category = ExpenseCategory.FOOD;

        Expense exp1 = new Expense(
                "Electronic equipments", new BigDecimal(125.99),
                ExpenseCategory.MISC,
                LocalDate.of(2024, 03, 17));
        Expense exp2 = new Expense(
                "Lunch", new BigDecimal(5.99),
                ExpenseCategory.FOOD,
                LocalDate.of(2024, 03, 8));

        Expense exp3 = new Expense(
                "Dinner", new BigDecimal(12.99),
                ExpenseCategory.FOOD,
                LocalDate.of(2024, 03, 21));

        List<Expense> toAdd = Arrays.asList(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()), exp1, exp2, exp3);

        repo.saveAll(toAdd);

       // List<ExpenseDto> contResult = Arrays.asList(service.mapDto(exp2), service.mapDto(exp3));
        mockMvc.perform(get("/expenses").param("from", from.toString())
                .param("to", to.toString())
                .param("category", category.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].description").value("Lunch"))
                .andExpect(jsonPath("$[1].description").value("Dinner"))
                .andExpect(jsonPath("$[0].amount").value(5.99))
                .andExpect(jsonPath("$[1].amount").value(12.99))
                .andExpect(jsonPath("$[0].category").value(ExpenseCategory.FOOD.name()))
                .andExpect(jsonPath("$[1].category").value(ExpenseCategory.FOOD.name()))
                .andExpect(jsonPath("$[0].date").value(LocalDate.of(2024, 03, 8).toString()))
                .andExpect(jsonPath("$[1].date").value(LocalDate.of(2024, 03, 21).toString()));

        List<Expense> repoResult = repo.filteredSearch(from, to, category.name());

        assertTrue(repoResult.size()==2);
        assertEquals("Lunch", repoResult.get(0).getDescription());
        assertEquals("Dinner", repoResult.get(1).getDescription());
        assertEquals(new BigDecimal("5.99"), repoResult.get(0).getAmount());
        assertEquals(new BigDecimal("12.99"), repoResult.get(1).getAmount());
        assertEquals(category, repoResult.get(0).getCategory());
        assertEquals(category, repoResult.get(1).getCategory());
        assertEquals(LocalDate.of(2024, 03, 8), repoResult.get(0).getDate());
        assertEquals(LocalDate.of(2024, 03, 21), repoResult.get(1).getDate());

    }

    @Test
    void testFilteredExpenses_NotFound() throws Exception {
        LocalDate from =  LocalDate.of(2024, 03, 01);
        LocalDate to = LocalDate.of(2024, 03, 31);
        ExpenseCategory category = ExpenseCategory.GROCERY;

        Expense exp1 = new Expense(
                "Electronic equipments", new BigDecimal(125.99),
                ExpenseCategory.MISC,
                LocalDate.of(2024, 03, 17));
        Expense exp2 = new Expense(
                "Lunch", new BigDecimal(5.99),
                ExpenseCategory.FOOD,
                LocalDate.of(2024, 03, 8));

        Expense exp3 = new Expense(
                "Dinner", new BigDecimal(12.99),
                ExpenseCategory.FOOD,
                LocalDate.of(2024, 03, 21));

        List<Expense> toAdd = Arrays.asList(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()), exp1, exp2, exp3);

        repo.saveAll(toAdd);

        mockMvc.perform(get("/expenses").param("from", from.toString())
                        .param("to", to.toString())
                        .param("category", category.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        List<Expense> repoResult = repo.filteredSearch(from, to, category.name());

        assertTrue(repoResult.isEmpty());
    }

    @Test
    void testCategorySum() throws Exception {
        int year = 2024;
        int month = 3;

        Expense exp1 = new Expense(
                "Electronic equipments", new BigDecimal(126),
                ExpenseCategory.MISC,
                LocalDate.of(2024, 3, 7));
        Expense exp2 = new Expense(
                "Gardening stuff", new BigDecimal(37),
                ExpenseCategory.MISC,
                LocalDate.of(2024, 3, 12));

        Expense exp3 = new Expense(
                "Visited Pune", new BigDecimal(55),
                ExpenseCategory.TRAVEL,
                LocalDate.of(2024, 3, 5));

        Expense exp4 = new Expense(
                "Visited Munich", new BigDecimal(3699),
                ExpenseCategory.TRAVEL,
                LocalDate.of(2024, 3, 19));

        List<Expense> toAdd = Arrays.asList(new Expense(expenseDto.getDescription(), expenseDto.getAmount(),
                expenseDto.getCategory(), expenseDto.getDate()), exp1, exp2, exp3, exp4);

        repo.saveAll(toAdd);

        mockMvc.perform(get("/expenses/summary")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("MISC"))
                .andExpect(jsonPath("$[1].category").value("TRAVEL"))
                .andExpect(jsonPath("$[0].sum").value(163))
                .andExpect(jsonPath("$[1].sum").value(3754));

        List<Object[]> result = repo.categorySum(year, month);

        assertTrue(result.size()==2);
        assertEquals(ExpenseCategory.MISC.name(), (String) result.get(0)[0]);
        assertEquals(ExpenseCategory.TRAVEL.name(), (String) result.get(1)[0]);
        assertEquals(new BigDecimal("163.00"), result.get(0)[1]);
        assertEquals(new BigDecimal("3754.00"), result.get(1)[1]);

    }

}
