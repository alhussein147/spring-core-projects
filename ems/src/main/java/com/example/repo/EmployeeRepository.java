package com.example.repo;

import com.example.models.Employee;

import java.util.List;
public interface EmployeeRepository {
    void save(Employee employee);
    List<Employee> findAll();
    Employee findById(int id);
}
