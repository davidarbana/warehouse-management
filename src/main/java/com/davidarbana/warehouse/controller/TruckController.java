package com.davidarbana.warehouse.controller;

import com.davidarbana.warehouse.dto.request.TruckRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.service.impl.TruckServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trucks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
@Tag(name = "Trucks")
public class TruckController {

    private final TruckServiceImpl truckService;

    @GetMapping
    public ResponseEntity<List<ResponseDtos.TruckResponse>> getAll() {
        return ResponseEntity.ok(truckService.getAllTrucks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDtos.TruckResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(truckService.getTruckById(id));
    }

    @PostMapping
    public ResponseEntity<ResponseDtos.TruckResponse> create(@Valid @RequestBody TruckRequest.Create request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(truckService.createTruck(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDtos.TruckResponse> update(@PathVariable Long id,
                                                              @RequestBody TruckRequest.Update request) {
        return ResponseEntity.ok(truckService.updateTruck(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        truckService.deleteTruck(id);
        return ResponseEntity.noContent().build();
    }
}
