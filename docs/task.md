# Junior Backend Developer Assignment

**Weather Temperature Service (AWS Lambda)**

## Goal

The goal of this assignment is to assess:

- code organization, readability, and reasoning
- object-oriented design fundamentals
- ability to consume external APIs
- basic AWS knowledge

You may use any online resources. The important part is that you **understand and can explain** the solution you submit.

---

## Task 1. Basic Implementation

### Functional Requirements

Using **your own AWS Free Tier account**, prepare an **AWS Lambda** function that:

- Returns the **current temperature in Wrocław**
- Uses the **Open-Meteo API** (https://open-meteo.com/) as a weather data source
- Returns the result in a structured format (e.g. JSON)

You may choose **any programming language** supported by AWS Lambda.

### Design Requirements

Your solution **must follow object-oriented design principles**.

The code should clearly contain:

- separation of **business logic** and **communication with external APIs**
- a Lambda handler whose responsibility is **limited to orchestration** (no business logic)

### Business Logic Requirement

In addition to the numeric temperature, return a temperature category based on the following rules:

| Temperature range | Category |
|-------------------|----------|
| < 0°C             | Freezing |
| 0–10°C            | Cold     |
| 10–20°C           | Mild     |
| 20–30°C           | Warm     |
| > 30°C            | Hot      |

The temperature classification logic:

- must not be placed directly in the Lambda handler
- should be isolated in business logic

### Documentation & Evidence

Provide a public GitHub repository containing:

1. All source code
2. A `README.md` with:
   a. brief description of your solution
   b. explanation of key design decisions
   c. short note on how the solution could be unit tested without calling the real API
3. A `doc/` directory containing screenshots:
   a. confirmation that the Lambda was created in AWS
   b. sample Lambda test execution with returned results

---

## Task 2. Extension

Extend the Lambda so that:

- the city name is provided as an input parameter
- the Lambda returns the current temperature for the given city

Provide additional screenshots presenting sample test events for at least two different cities.

---

## Task 3. Endpoint Exposure

Enable a **Lambda Function URL** for your AWS Lambda so that:

- the function can be invoked via HTTP
- the city name is provided as a **GET query parameter**
- the response returns the current temperature (and temperature category) for the given city

Provide:

- the publicly accessible URL
- the name of the GET parameter used in requests
- example requests and responses (screenshots or README section)

---

## Task 4. Design Reflection

Imagine that in the future:

- another weather provider might be added

In 1–2 short paragraphs, describe:

- how your current design supports or limits such changes
- what you would improve if you had more time

No additional implementation is required for this task.

---

## Evaluation Criteria

During review we will focus on:

- clarity and structure of the code
- correctness of the solution
- object-oriented design and separation of responsibilities
- readability and maintainability
- ability to explain design choices and trade-offs

---

## Notes

- This is a homework assignment, not a production system.
- We value clarity and reasoning more than advanced AWS features.
- If something is unclear, make a reasonable assumption and explain it in the README.