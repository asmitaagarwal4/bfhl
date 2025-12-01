package com.example.bfhl;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class BfhlApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(BfhlApplication.class, args);
    }

    @Bean
    public static RestTemplate restTemplate() {
        return new RestTemplate();
    }

    private final RestTemplate restTemplate;

    public BfhlApplication(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        System.out.println("Application Started");

        try {
            String name = "Asmita Agarwal";
            String regNo = "22BSA10015";
            String email = "asmitaagarwal2022@vitbhopal.ac.in";

            String firstUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            FirstApiRequest request1 = new FirstApiRequest(name, regNo, email);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<FirstApiRequest> entity1 = new HttpEntity<>(request1, headers1);

            System.out.println("\nSending 1st request...");

            ResponseEntity<FirstApiResponse> response1 = restTemplate.exchange(
                    firstUrl,
                    HttpMethod.POST,
                    entity1,
                    FirstApiResponse.class
            );

            FirstApiResponse body1 = response1.getBody();

            if (body1 == null || body1.getWebhook() == null) {
                System.err.println("Error: Received empty response or missing webhook URL.");
                return;
            }

            System.out.println("Success! Access Token and Webhook URL received: " + body1.getWebhook());

            String Query = "WITH EmployeeTotalSalary AS (\n" +
                    "    SELECT\n" +
                    "        E.EMP_ID,\n" +
                    "        E.FIRST_NAME,\n" +
                    "        E.LAST_NAME,\n" +
                    "        E.DOB,\n" +
                    "        E.DEPARTMENT AS DEPARTMENT_ID,\n" +
                    "        SUM(CASE WHEN DAYOFMONTH(P.PAYMENT_TIME) != 1 THEN P.AMOUNT ELSE 0 END) AS Total_Salary\n" +
                    "    FROM\n" +
                    "        EMPLOYEE E\n" +
                    "    JOIN\n" +
                    "        PAYMENTS P ON E.EMP_ID = P.EMP_ID\n" +
                    "    GROUP BY\n" +
                    "        E.EMP_ID\n" +
                    "),\n" +
                    "DepartmentMaxSalary AS (\n" +
                    "    SELECT\n" +
                    "        DEPARTMENT_ID,\n" +
                    "        MAX(Total_Salary) AS Max_Salary\n" +
                    "    FROM\n" +
                    "        EmployeeTotalSalary\n" +
                    "    GROUP BY\n" +
                    "        DEPARTMENT_ID\n" +
                    ")\n" +
                    "SELECT\n" +
                    "    D.DEPARTMENT_NAME,\n" +
                    "    ETS.Total_Salary AS SALARY,\n" +
                    "    CONCAT(ETS.FIRST_NAME, ' ', ETS.LAST_NAME) AS EMPLOYEE_NAME,\n" +
                    "    TIMESTAMPDIFF(YEAR, ETS.DOB, CURDATE()) AS AGE\n" +
                    "FROM\n" +
                    "    EmployeeTotalSalary ETS\n" +
                    "JOIN\n" +
                    "    DepartmentMaxSalary DMS\n" +
                    "    ON ETS.DEPARTMENT_ID = DMS.DEPARTMENT_ID AND ETS.Total_Salary = DMS.Max_Salary\n" +
                    "JOIN\n" +
                    "    DEPARTMENT D\n" +
                    "    ON ETS.DEPARTMENT_ID = D.DEPARTMENT_ID;\n";

            SecondApiRequest request2 = new SecondApiRequest(Query);

            HttpHeaders headers2 = new HttpHeaders();
            headers2.setContentType(MediaType.APPLICATION_JSON);
            headers2.set("Authorization", "Bearer " + body1.getAccessToken());

            HttpEntity<SecondApiRequest> entity2 = new HttpEntity<>(request2, headers2);

            System.out.println("\nSending 2nd POST request with text: " + Query);

            ResponseEntity<String> response2 = restTemplate.exchange(
                    body1.getWebhook(),
                    HttpMethod.POST,
                    entity2,
                    String.class
            );

            System.out.println("--- FINAL API RESPONSE ---");
            System.out.println(response2.getBody());

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }
}


class FirstApiRequest {
    private String name;
    private String regNo;
    private String email;

    public FirstApiRequest(String name, String regNo, String email) {
        this.name = name;
        this.regNo = regNo;
        this.email = email;
    }

    public String getName() { return name; }
    public String getRegNo() { return regNo; }
    public String getEmail() { return email; }
}

class FirstApiResponse {
    private String webhook;
    private String accessToken;

    public String getWebhook() { return webhook; }
    public void setWebhook(String webhook) { this.webhook = webhook; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
}

class SecondApiRequest {
    private String finalQuery;

    public SecondApiRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() { return finalQuery; }
}