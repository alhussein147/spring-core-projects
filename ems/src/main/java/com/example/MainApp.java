package com.example;

import com.example.audit.AuditLogger;
import com.example.config.AppConfig;
import com.example.config.ApplicationSettings;
import com.example.models.Employee;
import com.example.services.EmployeeService;
import com.example.services.InvalidEmployeeException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getEnvironment().setActiveProfiles("dev");
        context.register(AppConfig.class);
        context.refresh();

        try {
            EmployeeService employeeService = context.getBean(EmployeeService.class);
            EmployeeService sameEmployeeService = context.getBean(EmployeeService.class);
            System.out.println("# EmployeeService singleton reused? " + (employeeService == sameEmployeeService));

            System.out.println("# Adding valid employee \n");
            employeeService.addEmployee(new Employee(1, "Amina Hassan", "Engineering", 1_000));

            System.out.println("# Adding invalid employee \n");
            try {
                employeeService.addEmployee(new Employee(2, "   ", "Finance", -100));
            } catch (InvalidEmployeeException exception) {
                System.out.println("error adding employee " + exception.getMessage());
            }

            System.out.println("# Raise within allowed limit\n");
            employeeService.giveRaise(1, 10);

            System.out.println("# Raise exceeding allowed limit\n");
            try {
                employeeService.giveRaise(1, 20);
            } catch (IllegalArgumentException exception) {
                System.out.println("Raise rejected: " + exception.getMessage());
            }

            System.out.println("# Prototype AuditLogger instances\n");
            context.getBean(AuditLogger.class).log("First direct prototype request");
            context.getBean(AuditLogger.class).log("Second direct prototype request");

            System.out.println("# Employees \n");
            employeeService.getAllEmployees().forEach(e -> System.out.println(e));

            ApplicationSettings settings = context.getBean(ApplicationSettings.class);
            System.out.println("Injected configuration\n");
            System.out.println("company.name = " + settings.getCompanyName());
            System.out.println("company.currency = " + settings.getCompanyCurrency());
            System.out.println("notification.retry-count = " + settings.getNotificationRetryCount());
            System.out.println("raise.max-percentage = " + settings.getMaxRaisePercentage());
        } finally {
            System.out.println("\n Closing Spring context");
            context.close();
        }

    }
}
