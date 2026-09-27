package com.example.services;

import com.example.audit.AuditLogger;
import com.example.notifiy.NotificationManager;
import com.example.repo.EmployeeRepository;
import com.example.models.Employee;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.List;

@Service
@DependsOn("notificationManager")
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final NotificationManager notificationManager;
    private final EmployeeValidator employeeValidator;
    private final ObjectProvider<AuditLogger> auditLoggerProvider;

    @Value("${raise.max-percentage}")
    private double maxRaisePercentage;

    @Value("${company.currency}")
    private String currency;

    @Autowired
    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               NotificationManager notificationManager,
                               EmployeeValidator employeeValidator,
                               ObjectProvider<AuditLogger> auditLoggerProvider) {
        this.employeeRepository = employeeRepository;
        this.notificationManager = notificationManager;
        this.employeeValidator = employeeValidator;
        this.auditLoggerProvider = auditLoggerProvider;

    }

    @PostConstruct
    public void initialize() {
        System.out.println("EmployeeServiceImpl initialized (singleton instance "
                + System.identityHashCode(this) + ")");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("EmployeeServiceImpl destroyed (singleton instance "
                + System.identityHashCode(this) + ")");
    }

    @Override
    public Employee getEmployeeById(int id) {
        audit("Looked up employee " + id);
        return employeeRepository.findById(id);
    }

    @Override
    public List<Employee> getAllEmployees() {
        audit("Listed all employees");
        return employeeRepository.findAll();
    }

    @Override
    public void addEmployee(Employee employee) {
        employeeValidator.validate(employee);
        employeeRepository.save(employee);
        audit("Added employee " + employee.getId());
        notificationManager.send("Employee " + employee.getId() + " was added.");
    }

    @Override
    public void giveRaise(int employeeId, double percentage) {
        Employee employee = employeeRepository.findById(employeeId);
        if (employee == null) {
            throw new IllegalArgumentException("Employee " + employeeId + " was not found");
        }

        employeeValidator.validate(employee);
        if (percentage < 0 || percentage > maxRaisePercentage) {
            throw new IllegalArgumentException("Raise percentage must be between 0 and "
                    + maxRaisePercentage + "%");
        }

        double newSalary = employee.getSalary() * (1 + percentage / 100);
        employee.setSalary(newSalary);
        audit("Gave employee " + employeeId + " a " + percentage + "% raise");
        notificationManager.send("Employee " + employeeId + " received a " + percentage
                + "% raise. New salary: " + newSalary + " " + currency);
    }

    private void audit(String message) {
        auditLoggerProvider.getObject().log(message);
    }
}
