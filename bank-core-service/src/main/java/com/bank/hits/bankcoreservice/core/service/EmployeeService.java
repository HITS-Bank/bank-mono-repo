package com.bank.hits.bankcoreservice.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.core.entity.Employee;
import com.bank.hits.bankcoreservice.core.repository.EmployeeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public boolean isEmployeeBlocked(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElse(employeeRepository.save(new Employee()));
        return employee.isBlocked();
    }

    public void blockEmployee(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElse(employeeRepository.save(new Employee()));
        employee.setBlocked(true);
        employeeRepository.save(employee);
    }

    public void unblockEmployee(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElse(employeeRepository.save(new Employee()));
        employee.setBlocked(false);
        employeeRepository.save(employee);
    }
}
