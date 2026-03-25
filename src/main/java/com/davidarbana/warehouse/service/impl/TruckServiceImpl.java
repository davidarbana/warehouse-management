package com.davidarbana.warehouse.service.impl;

import com.davidarbana.warehouse.dto.request.TruckRequest;
import com.davidarbana.warehouse.dto.response.ResponseDtos;
import com.davidarbana.warehouse.entity.Truck;
import com.davidarbana.warehouse.exception.InvalidOperationException;
import com.davidarbana.warehouse.exception.ResourceNotFoundException;
import com.davidarbana.warehouse.repository.TruckRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TruckServiceImpl {

    private final TruckRepository truckRepository;

    public List<ResponseDtos.TruckResponse> getAllTrucks() {
        return truckRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ResponseDtos.TruckResponse getTruckById(Long id) {
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));
        return mapToResponse(truck);
    }

    public ResponseDtos.TruckResponse createTruck(TruckRequest.Create request) {
        if (truckRepository.existsByChassisNumber(request.getChassisNumber())) {
            throw new InvalidOperationException("Chassis number already in use");
        }
        if (truckRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new InvalidOperationException("License plate already in use");
        }

        Truck truck = Truck.builder()
                .chassisNumber(request.getChassisNumber())
                .licensePlate(request.getLicensePlate())
                .containerVolume(request.getContainerVolume())
                .build();

        truckRepository.save(truck);
        return mapToResponse(truck);
    }

    public ResponseDtos.TruckResponse updateTruck(Long id, TruckRequest.Update request) {
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));

        if (request.getChassisNumber() != null && !truck.getChassisNumber().equalsIgnoreCase(request.getChassisNumber())) {
            if (truckRepository.existsByChassisNumber(request.getChassisNumber())) {
                throw new InvalidOperationException("Chassis number already in use");
            }
            truck.setChassisNumber(request.getChassisNumber());
        }
        if (request.getLicensePlate() != null && !truck.getLicensePlate().equalsIgnoreCase(request.getLicensePlate())) {
            if (truckRepository.existsByLicensePlate(request.getLicensePlate())) {
                throw new InvalidOperationException("License plate already in use");
            }
            truck.setLicensePlate(request.getLicensePlate());
        }
        if (request.getContainerVolume() != null) truck.setContainerVolume(request.getContainerVolume());

        truckRepository.save(truck);
        return mapToResponse(truck);
    }

    public void deleteTruck(Long id) {
        Truck truck = truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));
        truckRepository.delete(truck);
    }

    public Truck findById(Long id) {
        return truckRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Truck not found: " + id));
    }

    public ResponseDtos.TruckResponse mapToResponse(Truck truck) {
        return ResponseDtos.TruckResponse.builder()
                .id(truck.getId())
                .chassisNumber(truck.getChassisNumber())
                .licensePlate(truck.getLicensePlate())
                .containerVolume(truck.getContainerVolume())
                .build();
    }
}
