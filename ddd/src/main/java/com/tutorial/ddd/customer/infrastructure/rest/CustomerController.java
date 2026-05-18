package com.tutorial.ddd.customer.infrastructure.rest;

import com.tutorial.ddd.customer.application.CustomerApplicationService;
import com.tutorial.ddd.customer.application.command.CreateCustomerCommand;
import com.tutorial.ddd.customer.domain.Customer;
import com.tutorial.ddd.customer.infrastructure.rest.dto.CreateCustomerRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerApplicationService service;

    public CustomerController(CustomerApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        var customer = service.create(new CreateCustomerCommand(request.name(), request.email()));
        return ResponseEntity.created(URI.create("/api/customers/" + customer.id().value())).body(CustomerResponse.from(customer));
    }

    @GetMapping
    public List<CustomerResponse> getAll() {
        return service.findAll().stream().map(CustomerResponse::from).toList();
    }

    public record CustomerResponse(String id, String name, String email, boolean active) {
        static CustomerResponse from(Customer customer) {
            return new CustomerResponse(customer.id().value(), customer.name(), customer.email(), customer.isActive());
        }
    }
}

