package com.cns.dh.controller;

import com.cns.dh.dto.DhRequest;
import com.cns.dh.dto.MitmRequest;
import com.cns.dh.dto.HistoryResponse;
import com.cns.dh.service.DhService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173")
public class SimulationController {
    private final DhService service;

    public SimulationController(DhService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok", "service", "dh-simulator");
    }

    @PostMapping("/simulations/diffie-hellman")
    public Map<String, Object> normal(@RequestBody DhRequest request) {
        try {
            return service.simulate(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PostMapping("/simulations/mitm")
    public Map<String, Object> mitm(@RequestBody MitmRequest request) {
        try {
            return service.simulateMitm(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @GetMapping("/simulations/history")
    public List<HistoryResponse> history() {
        return service.history();
    }
}
