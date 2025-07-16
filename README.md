# Mini Expense Tracker

## Overview

Mini Expense Tracker is a Spring Boot application that allowa users to track and manage their expenses. The application also provides monthly summaries of total expenses in each category.
it provides RESTful APIs for interacting with expense data.


## Technologies
* Java 17
* Spring Boot
* Spring Data JPA
* Maven
* H2 Database
* JUnit & Mockito for testing


## Features
* Add expenses
* Update and delete expenses
* Fetch expenses by expense id
* Retrieve list of expenses between specific dates
* Generate summaries of total expenses in each category for any given month


## Gettiing Started
### Prerequisites
* Java 17+
* Maven

### Installation
1. **Clone the repository**

    ```
    git clone https://github.com/mishrahul/Mini-Expense-Tracker.git
    cd Mini-Expense-Tracker
    ```

2. **Building the project**
    ```
    mvn clean install
    ```

3. **Running the application**
    ```
    mvn spring-boot:run
    ```

The applicatiion runs on the post **8080** by default and can be accessed at **https://localhost:8080**


## API Endpoints
### To add an expense entry
1. Endpoint **POST /expenses**
2. Request Body
   ```
   {
      "description": "Lunch",
      "amount": 10.00,
      "category": "FOOD",
      "date": "2025-06-01"
    }
   ```

### To update an expense entry
1. Endpoint: **PUT /expenses**

2. Request Body
  ```
     {
      "description": "Lunch",
      "amount": 10.00,
      "category": "FOOD",
      "date": "2025-06-01"
    }
   ```

### To retrieve expense data between two dates for a particular category
1. Endpoint: **GET /expenses**

2. Request parametes

   **from** : Date in the standard ISO 8016 format (*e.g. yyyy-MM-dd*)
   **to** : Date in standard ISO 8061 format
   **category** : Capitalized expense category (*e.g. GROCERY, FOOD, MEDICAL, TRAVEL*)
   
3. Example URL
   ```
   https://localhost:8080/expenses?from=yyyy-MM-dd&to=yyyy-MM-dd&category=FOOD
   ```

### To get categoriize wise sum of expenses for a particular month of a year
1. Endpoint: **GET /expenses/summary**

2. Request parrameters

   **year** : The year for the summary
   **month** : The month for the summary

3. Example URL
   ```
   https://localhost:8080/expenses/summary?year=2025&month=06
   ```

