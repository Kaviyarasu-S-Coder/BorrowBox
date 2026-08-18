package com.borrowbox.repository;

import com.borrowbox.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
public class EntityMappingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private BorrowTransactionRepository borrowTransactionRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    @DisplayName("Should successfully persist User, Category, Item, BorrowRequest, and Transaction")
    void testEntityPersistenceAndRelationships() {
        // 1. Create Users
        User owner = User.builder()
                .email("owner@borrowbox.test")
                .password("hashed_password")
                .fullName("Jane Doe")
                .location("Indiranagar, Bangalore")
                .roles(new HashSet<>(Collections.singleton(Role.ROLE_USER)))
                .build();
        owner = userRepository.save(owner);

        User borrower = User.builder()
                .email("borrower@borrowbox.test")
                .password("hashed_password")
                .fullName("John Smith")
                .location("Koramangala, Bangalore")
                .roles(new HashSet<>(Collections.singleton(Role.ROLE_USER)))
                .build();
        borrower = userRepository.save(borrower);

        // 2. Create Category
        Category category = Category.builder()
                .name("Electronics")
                .slug("electronics")
                .icon("Cpu")
                .build();
        category = categoryRepository.save(category);

        // 3. Create Item
        Item item = Item.builder()
                .owner(owner)
                .category(category)
                .subCategory("Camera")
                .title("Canon EOS 1500D DSLR")
                .description("24.1MP DSLR camera with 18-55mm lens.")
                .condition(ItemCondition.GOOD)
                .estimatedValue(BigDecimal.valueOf(35000))
                .depositAmount(BigDecimal.valueOf(2000))
                .location("Indiranagar, Bangalore")
                .status(ItemStatus.AVAILABLE)
                .build();
        item = itemRepository.save(item);

        // 4. Create BorrowRequest
        LocalDate start = LocalDate.now().plusDays(2);
        LocalDate end = LocalDate.now().plusDays(5);
        BorrowRequest request = BorrowRequest.builder()
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(start)
                .endDate(end)
                .message("Need for weekend trip")
                .purpose("Photography")
                .status(RequestStatus.ACCEPTED)
                .build();
        request = borrowRequestRepository.save(request);

        // 5. Create BorrowTransaction
        BorrowTransaction transaction = BorrowTransaction.builder()
                .borrowRequest(request)
                .item(item)
                .borrower(borrower)
                .owner(owner)
                .startDate(start)
                .endDate(end)
                .status(TransactionStatus.UPCOMING)
                .pickupCode("123456")
                .returnCode("654321")
                .depositHeld(item.getDepositAmount())
                .build();
        transaction = borrowTransactionRepository.save(transaction);

        // 6. Assertions
        assertThat(owner.getId()).isNotNull();
        assertThat(item.getId()).isNotNull();
        assertThat(request.getId()).isNotNull();
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getPickupCode()).isEqualTo("123456");

        // 7. Test Overlap Query
        List<BorrowTransaction> conflicts = borrowTransactionRepository.findOverlappingActiveTransactions(
                item.getId(),
                List.of(TransactionStatus.UPCOMING, TransactionStatus.BORROWED),
                start,
                end
        );
        assertThat(conflicts).hasSize(1);
    }
}
