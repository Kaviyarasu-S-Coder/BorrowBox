package com.borrowbox;

import com.borrowbox.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected MessageRepository messageRepository;

    @Autowired
    protected ConversationRepository conversationRepository;

    @Autowired
    protected DisputeRepository disputeRepository;

    @Autowired
    protected ReportRepository reportRepository;

    @Autowired
    protected FavoriteRepository favoriteRepository;

    @Autowired
    protected RatingRepository ratingRepository;

    @Autowired
    protected NotificationRepository notificationRepository;

    @Autowired
    protected TransactionConditionRepository conditionRepository;

    @Autowired
    protected BorrowTransactionRepository transactionRepository;

    @Autowired
    protected BorrowRequestRepository borrowRequestRepository;

    @Autowired
    protected ItemRepository itemRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected UserRepository userRepository;

    @BeforeEach
    public void cleanupDatabase() {
        if (disputeRepository != null) disputeRepository.deleteAll();
        if (reportRepository != null) reportRepository.deleteAll();
        if (messageRepository != null) messageRepository.deleteAll();
        if (conversationRepository != null) conversationRepository.deleteAll();
        if (favoriteRepository != null) favoriteRepository.deleteAll();
        if (ratingRepository != null) ratingRepository.deleteAll();
        if (notificationRepository != null) notificationRepository.deleteAll();
        if (conditionRepository != null) conditionRepository.deleteAll();
        if (transactionRepository != null) transactionRepository.deleteAll();
        if (borrowRequestRepository != null) borrowRequestRepository.deleteAll();
        if (itemRepository != null) itemRepository.deleteAll();
        if (categoryRepository != null) categoryRepository.deleteAll();
        if (userRepository != null) userRepository.deleteAll();
    }
}
