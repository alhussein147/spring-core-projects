package com.example.services;

import com.example.models.Employee;
import org.springframework.stereotype.Service;

@Service
public class EmployeeValidator {
     public void validate(Employee employee) throws InvalidEmployeeException {
        if (employee == null) {
            throw new InvalidEmployeeException("Employee is required");
        }

        if (employee.getName() == null || employee.getName().isBlank())  {
            throw new InvalidEmployeeException("Employee name is required");
        }

        if (employee.getSalary() < 0)  {
            throw new InvalidEmployeeException("Employee salary cannot be negative");
        }
    }
}
