package com.davidarbana.warehouse.config;

import com.davidarbana.warehouse.entity.InventoryItem;
import com.davidarbana.warehouse.entity.Truck;
import com.davidarbana.warehouse.entity.User;
import com.davidarbana.warehouse.enums.Role;
import com.davidarbana.warehouse.repository.InventoryItemRepository;
import com.davidarbana.warehouse.repository.TruckRepository;
import com.davidarbana.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Seeds some default data so the app is usable out of the box
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final TruckRepository truckRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsers();
        }
        if (inventoryItemRepository.count() == 0) {
            seedItems();
        }
        if (truckRepository.count() == 0) {
            seedTrucks();
        }
    }

    private void seedUsers() {
        userRepository.save(User.builder().fullName("System Admin").username("admin")
                .password(passwordEncoder.encode("admin123")).email("admin@warehouse.com")
                .role(Role.SYSTEM_ADMIN).enabled(true).build());

        userRepository.save(User.builder().fullName("John Manager").username("manager")
                .password(passwordEncoder.encode("manager123")).email("manager@warehouse.com")
                .role(Role.WAREHOUSE_MANAGER).enabled(true).build());

        userRepository.save(User.builder().fullName("Jane Client").username("client")
                .password(passwordEncoder.encode("client123")).email("client@warehouse.com")
                .role(Role.CLIENT).enabled(true).build());

        log.info("Seeded default users (admin / manager / client)");
    }

    private void seedItems() {
        inventoryItemRepository.save(new InventoryItem(null, "Industrial Drill", 50, 299.99, 2.5));
        inventoryItemRepository.save(new InventoryItem(null, "Safety Helmet", 200, 24.99, 0.3));
        inventoryItemRepository.save(new InventoryItem(null, "Steel Pipe (3m)", 100, 89.99, 1.8));
        inventoryItemRepository.save(new InventoryItem(null, "Work Gloves", 500, 9.99, 0.05));
        log.info("Seeded inventory items");
    }

    private void seedTrucks() {
        truckRepository.save(new Truck(null, "CH-001-2024", "AA-123-BB", 50.0));
        truckRepository.save(new Truck(null, "CH-002-2024", "CC-456-DD", 75.0));
        truckRepository.save(new Truck(null, "CH-003-2024", "EE-789-FF", 100.0));
        log.info("Seeded trucks");
    }
}
