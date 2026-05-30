package com.abc.pricing.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import com.abc.pricing.domain.*;
import com.abc.pricing.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProductRepository productRepository;
    private final CompetitorPriceRepository competitorPriceRepository;
    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final PricingRuleRepository pricingRuleRepository;

    @Bean
    @Profile("local")
    public CommandLineRunner initData() {
        return args -> {
            log.info("Initializing sample data...");

            createProducts();
            createCompetitorPrices();
            createFestivals();
            createUsers();
            createDynamicRules();
        };
    }

    // ================= PRODUCTS =================

    private void createProducts() {

        saveProduct("prod-001", "iPhone 17 Pro", "Latest smartphone",
                new BigDecimal("135900"), "electronics", 300, 500);

        saveProduct("prod-002", "Samsung Galaxy S25", "Flagship Android phone",
                new BigDecimal("119999"), "electronics", 250, 400);

        saveProduct("prod-003", "Sony Bravia 55 OLED", "4K Smart TV",
                new BigDecimal("159999"), "electronics", 120, 200);

        saveProduct("prod-004", "Nike Air Zoom", "Running shoes",
                new BigDecimal("8999"), "fashion", 500, 800);

        saveProduct("prod-005", "Levi's Jeans", "Slim fit denim",
                new BigDecimal("3499"), "fashion", 600, 1000);

        saveProduct("prod-006", "Dyson V12 Vacuum", "Premium vacuum cleaner",
                new BigDecimal("54999"), "home_appliances", 80, 150);

        saveProduct("prod-007", "LG Front Load Washer", "7kg washing machine",
                new BigDecimal("38999"), "home_appliances", 70, 120);

        log.info("Created sample products");
    }

    private void saveProduct(String id, String name, String description,
                             BigDecimal price, String category,
                             int inventory, int total) {

        productRepository.findById(id)
                .switchIfEmpty(productRepository.save(Product.builder()
                        .id(id)
                        .name(name)
                        .description(description)
                        .basePrice(price)
                        .category(category)
                        .inventoryCount(inventory)
                        .totalInventory(total)
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()))
                .subscribe();
    }

    // ================= COMPETITOR PRICES =================

    private void createCompetitorPrices() {

        // iPhone
        saveCompetitorPrice("prod-001", "Amazon", new BigDecimal("130000"));
        saveCompetitorPrice("prod-001", "Flipkart", new BigDecimal("129500"));

        // Samsung
        saveCompetitorPrice("prod-002", "Amazon", new BigDecimal("115000"));
        saveCompetitorPrice("prod-002", "Reliance Digital", new BigDecimal("116500"));

        // TV
        saveCompetitorPrice("prod-003", "Croma", new BigDecimal("155000"));
        saveCompetitorPrice("prod-003", "Amazon", new BigDecimal("154000"));

        // Shoes
        saveCompetitorPrice("prod-004", "Myntra", new BigDecimal("7999"));

        // Jeans
        saveCompetitorPrice("prod-005", "Ajio", new BigDecimal("2999"));

        log.info("Created competitor prices");
    }

    private void saveCompetitorPrice(String productId, String competitor, BigDecimal price) {
        competitorPriceRepository.save(CompetitorPrice.builder()
                .id(UUID.randomUUID().toString())
                .productId(productId)
                .competitorName(competitor)
                .price(price)
                .lastUpdated(LocalDateTime.now())
                .build())
                .subscribe();
    }

    // ================= FESTIVALS =================

    private void createFestivals() {
        int year = 2026;

        saveFestival("Republic Day", LocalDate.of(year, 1, 26), LocalDate.of(year, 1, 26), new BigDecimal(20));
        saveFestival("Valentine Sale", LocalDate.of(year, 2, 7), LocalDate.of(year, 2, 14), new BigDecimal(10));

        saveFestival("Holi Sale", LocalDate.of(year, 3, 3), LocalDate.of(year, 3, 5), new BigDecimal(20));

        saveFestival("Ramzan Sale", LocalDate.of(year, 3, 20), LocalDate.of(year, 4, 10), new BigDecimal(15));

        saveFestival("Independence Day", LocalDate.of(year, 8, 15), LocalDate.of(year, 8, 15), new BigDecimal(12));

        saveFestival("Navratri Sale", LocalDate.of(year, 10, 10), LocalDate.of(year, 10, 18), new BigDecimal(25));

        saveFestival("Diwali Sale", LocalDate.of(year, 11, 8), LocalDate.of(year, 11, 15), new BigDecimal(30));

        saveFestival("New Year Sale", LocalDate.of(year, 12, 28), LocalDate.of(year + 1, 1, 2), new BigDecimal(18));

        log.info("Created Festivals");
    }

    private void saveFestival(String name, LocalDate start, LocalDate end, BigDecimal discount) {
        festivalRepository.save(Festival.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .startDate(start)
                .endDate(end)
                .discountPercentage(discount)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build())
                .subscribe();
    }

    // ================= USERS =================

    private void createUsers() {

        saveUser("user-001", "Harish", "harish123@gmail.com", MembershipTier.STANDARD);
        saveUser("user-002", "Umesh", "umesh21apr@gmail.com", MembershipTier.GOLD);
        saveUser("user-003", "Ankur", "ankur@gmail.com", MembershipTier.PLATINUM);

        saveUser("user-004", "Riya", "riya@gmail.com", MembershipTier.STANDARD);
        saveUser("user-005", "Amit", "amit@gmail.com", MembershipTier.GOLD);
        saveUser("user-006", "Sneha", "sneha@gmail.com", MembershipTier.PLATINUM);
        saveUser("user-007", "Rahul", "rahul@gmail.com", MembershipTier.STANDARD);

        log.info("Created Users");
    }

    private void saveUser(String id, String username, String email, MembershipTier tier) {
        userRepository.save(User.builder()
                .id(id)
                .username(username)
                .email(email)
                .membershipTier(tier)
                .createdAt(LocalDateTime.now())
                .build())
                .subscribe();
    }

    // ================= DYNAMIC RULES =================

    private void createDynamicRules() {

        saveDynamicRule("Electronics Weekend Sale",
                "DYNAMIC_DISCOUNT",
                500,
                "{\"category\":\"electronics\",\"discountPercentage\":5}");

        saveDynamicRule("Low Inventory Surge Pricing",
                "SURGE_PRICING",
                900,
                "{\"inventoryThreshold\":100,\"priceIncreasePercentage\":10}");

        saveDynamicRule("Competitor Price Match",
                "COMPETITOR_MATCH",
                800,
                "{\"matchLowest\":true}");

        saveDynamicRule("Festival Boost Rule",
                "FESTIVAL_DISCOUNT",
                700,
                "{\"extraDiscount\":5}");

        log.info("Created Dynamic Rules");
    }

    private void saveDynamicRule(String name, String type, int priority, String config) {
        pricingRuleRepository.save(PricingRuleEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .description("Dynamic pricing rule")
                .ruleType(type)
                .priority(priority)
                .enabled(true)
                .configuration(config)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build())
                .subscribe();
    }
}