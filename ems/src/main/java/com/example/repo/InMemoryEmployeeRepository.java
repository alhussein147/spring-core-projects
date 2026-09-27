package com.example.repo;

import com.example.models.Employee;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Profile("dev")
public class InMemoryEmployeeRepository implements EmployeeRepository {

    ArrayList<Employee> employees = new ArrayList<>();

    @Override
    public void save(Employee employee) {
        employees.add(employee);
    }

    @Override
    public List<Employee> findAll() {
        return Collections.unmodifiableList(employees);
    }

    @Override
    public Employee findById(int id) {

        return employees.stream().filter(emp -> emp.getId() == id)
                .findFirst()
                .orElse(null);

    }
}
