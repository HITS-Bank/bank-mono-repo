package com.bank.hits.bankcoreservice.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ciklon.bank.bankcoreservice.core.entity.Employee;
import ru.ciklon.bank.bankcoreservice.core.repository.EmployeeRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public boolean isEmployeeBlocked(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        return employee.isBlocked();
    }

    public void blockEmployee(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setBlocked(true);
        employeeRepository.save(employee);
    }

    public void unblockEmployee(final UUID employeeId) {
        final Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Employee not found"));
        employee.setBlocked(false);
        employeeRepository.save(employee);
    }
}
