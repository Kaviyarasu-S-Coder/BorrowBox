package com.borrowbox.config;

import com.borrowbox.entity.*;
import com.borrowbox.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ItemRepository itemRepository;
    private final ItemImageRepository itemImageRepository;
    private final BorrowRequestRepository borrowRequestRepository;
    private final BorrowTransactionRepository borrowTransactionRepository;
    private final TransactionConditionRepository transactionConditionRepository;
    private final RatingRepository ratingRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final DisputeRepository disputeRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data ({} users). Skipping DataSeeder.", userRepository.count());
            return;
        }

        log.info("============================================================");
        log.info("STARTING BORROWBOX IDEMPOTENT SEED DATA ENGINE (PHASE 20)...");
        log.info("============================================================");

        // 1. Seed Users
        String encodedUserPass = passwordEncoder.encode("Password123!");
        String encodedAdminPass = passwordEncoder.encode("AdminPass123!");

        User admin = User.builder()
                .email("admin@borrowbox.com")
                .password(encodedAdminPass)
                .fullName("BorrowBox Platform Admin")
                .location("HQ Admin Center, Bangalore")
                .phone("+91 98765 00000")
                .bio("Official BorrowBox Platform Operations & Community Trust Administrator.")
                .roles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER))
                .isVerified(true)
                .isActive(true)
                .reputationScore(99.0)
                .averageRating(5.0)
                .ratingCount(25)
                .completedLendings(15)
                .completedBorrowings(12)
                .build();

        User sarah = User.builder()
                .email("sarah@borrowbox.test")
                .password(encodedUserPass)
                .fullName("Sarah Jenkins")
                .location("Indiranagar, Bangalore")
                .phone("+91 98765 11111")
                .bio("Passionate commercial photographer and maker. Happy to lend camera gear, lighting, and workshop tools.")
                .profileImageUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400")
                .roles(Set.of(Role.ROLE_USER))
                .isVerified(true)
                .isActive(true)
                .reputationScore(96.0)
                .averageRating(4.9)
                .ratingCount(18)
                .completedLendings(14)
                .completedBorrowings(6)
                .build();

        User alex = User.builder()
                .email("alex@borrowbox.test")
                .password(encodedUserPass)
                .fullName("Alex Morgan")
                .location("Koramangala, Bangalore")
                .phone("+91 98765 22222")
                .bio("DIY home renovator and weekend outdoor camper. Treat all borrowed equipment with top care.")
                .profileImageUrl("https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400")
                .roles(Set.of(Role.ROLE_USER))
                .isVerified(true)
                .isActive(true)
                .reputationScore(90.0)
                .averageRating(4.8)
                .ratingCount(12)
                .completedLendings(5)
                .completedBorrowings(10)
                .build();

        User priya = User.builder()
                .email("priya@borrowbox.test")
                .password(encodedUserPass)
                .fullName("Priya Patel")
                .location("HSR Layout, Bangalore")
                .phone("+91 98765 33333")
                .bio("Trekker, camper, and cycling enthusiast. Sharing quality gear to promote sustainable living.")
                .profileImageUrl("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400")
                .roles(Set.of(Role.ROLE_USER))
                .isVerified(true)
                .isActive(true)
                .reputationScore(94.0)
                .averageRating(5.0)
                .ratingCount(9)
                .completedLendings(8)
                .completedBorrowings(4)
                .build();

        User david = User.builder()
                .email("david@borrowbox.test")
                .password(encodedUserPass)
                .fullName("David Chen")
                .location("Whitefield, Bangalore")
                .phone("+91 98765 44444")
                .bio("Sound designer, musician, and tech hobbyist. Sharing AV equipment, synthesizers, and instruments.")
                .profileImageUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=400")
                .roles(Set.of(Role.ROLE_USER))
                .isVerified(false)
                .isActive(true)
                .reputationScore(86.0)
                .averageRating(4.5)
                .ratingCount(6)
                .completedLendings(4)
                .completedBorrowings(5)
                .build();

        userRepository.saveAll(List.of(admin, sarah, alex, priya, david));
        log.info("Seeded 5 demo users with credentials (admin@borrowbox.com / sarah@borrowbox.test / alex@borrowbox.test)");

        // 2. Seed Categories
        Category tools = Category.builder()
                .name("Power Tools & Hardware")
                .slug("power-tools")
                .description("Drills, saws, sanders, rotary hammers, pressure washers, and workshop gear.")
                .icon("Wrench")
                .isActive(true)
                .build();

        Category cameras = Category.builder()
                .name("Cameras & Photography")
                .slug("cameras-photography")
                .description("Mirrorless DSLRs, prime & zoom lenses, studio lighting, gimbals, and drones.")
                .icon("Camera")
                .isActive(true)
                .build();

        Category camping = Category.builder()
                .name("Camping & Outdoors")
                .slug("camping-outdoors")
                .description("Waterproof tents, sleeping bags, stoves, hiking backpacks, and outdoor lanterns.")
                .icon("Tent")
                .isActive(true)
                .build();

        Category music = Category.builder()
                .name("Musical Instruments")
                .slug("musical-instruments")
                .description("Guitars, keyboards, synthesizers, microphones, and audio amplifiers.")
                .icon("Music")
                .isActive(true)
                .build();

        Category garden = Category.builder()
                .name("Lawn & Garden")
                .slug("lawn-garden")
                .description("Lawn mowers, hedge trimmers, leaf blowers, and power garden sprayers.")
                .icon("Scissors")
                .isActive(true)
                .build();

        Category electronics = Category.builder()
                .name("Electronics & AV")
                .slug("electronics")
                .description("Laser projectors, PA sound systems, VR headsets, monitors, and party lights.")
                .icon("Laptop")
                .isActive(true)
                .build();

        categoryRepository.saveAll(List.of(tools, cameras, camping, music, garden, electronics));
        log.info("Seeded 6 root equipment categories.");

        // 3. Seed Items
        List<Item> items = new ArrayList<>();

        // Item 1: Sony A7 III (Sarah)
        Item item1 = Item.builder()
                .owner(sarah)
                .category(cameras)
                .subCategory("Mirrorless Cameras")
                .title("Sony Alpha A7 III Full-Frame Camera + 28-70mm Kit")
                .description("Full-frame 24.2MP mirrorless camera with 4K HDR video, 5-axis in-body image stabilization, dual SD card slots, and ultra-fast autofocus. Includes 2 original NP-FZ100 batteries, charger, neck strap, 64GB V60 SD card, and protective hard case.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(850.0))
                .depositAmount(BigDecimal.valueOf(5000.0))
                .estimatedValue(BigDecimal.valueOf(145000.0))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .borrowingRules("1. Store in the weather-sealed case when not actively shooting.\n2. Do not expose to salt water or heavy rain.\n3. Return with both batteries fully charged.")
                .location("Indiranagar 100ft Road, Bangalore")
                .borrowCount(16)
                .viewCount(240)
                .build();
        items.add(item1);

        // Item 2: DeWalt 20V Drill (Sarah)
        Item item2 = Item.builder()
                .owner(sarah)
                .category(tools)
                .subCategory("Cordless Drills")
                .title("DeWalt 20V MAX Cordless Hammer Drill & Impact Driver Combo")
                .description("High-performance brushless motor delivering 2,000 RPM. Ideal for concrete, masonry, wood, and metal drilling. Comes with two 4.0Ah XR batteries, fast charger, 30-piece drill and screwdriver bit set, and heavy-duty tool bag.")
                .condition(ItemCondition.GOOD)
                .status(ItemStatus.BORROWED)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(300.0))
                .depositAmount(BigDecimal.valueOf(1500.0))
                .estimatedValue(BigDecimal.valueOf(18500.0))
                .minBorrowDays(1)
                .maxBorrowDays(5)
                .borrowingRules("Clean chuck and bit holders after use. Do not submerge or leave outdoors overnight.")
                .location("Indiranagar 12th Main, Bangalore")
                .borrowCount(22)
                .viewCount(310)
                .build();
        items.add(item2);

        // Item 3: Coleman Tent (Priya)
        Item item3 = Item.builder()
                .owner(priya)
                .category(camping)
                .subCategory("Tents & Shelters")
                .title("Coleman Sundome 4-Person Waterproof Camping Tent")
                .description("WeatherTec system with patented welded floors and inverted seams to keep you dry. Sets up in under 10 minutes. Ground vents and large windows for superior airflow. Includes ground tarp, rainfly, stakes, and carry bag.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(400.0))
                .depositAmount(BigDecimal.valueOf(2000.0))
                .estimatedValue(BigDecimal.valueOf(9500.0))
                .minBorrowDays(2)
                .maxBorrowDays(7)
                .borrowingRules("Ensure tent is completely dry before folding into the carry bag. Wash ground stakes after camping.")
                .location("HSR Layout Sector 2, Bangalore")
                .borrowCount(11)
                .viewCount(190)
                .build();
        items.add(item3);

        // Item 4: Karcher Pressure Washer (Alex)
        Item item4 = Item.builder()
                .owner(alex)
                .category(garden)
                .subCategory("Pressure Washers")
                .title("Kärcher K3 High Pressure Washer 120 Bar")
                .description("Compact high-pressure cleaner with Quick Connect trigger gun, 6-meter high-pressure hose, Vario Power spray lance, and dirt blaster lance with rotating point jet. Perfect for cars, patios, decks, and exterior wall cleaning.")
                .condition(ItemCondition.GOOD)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(450.0))
                .depositAmount(BigDecimal.valueOf(2500.0))
                .estimatedValue(BigDecimal.valueOf(14000.0))
                .minBorrowDays(1)
                .maxBorrowDays(3)
                .borrowingRules("Ensure garden hose is running water before turning on the electric pump to prevent dry running.")
                .location("Koramangala 4th Block, Bangalore")
                .borrowCount(14)
                .viewCount(180)
                .build();
        items.add(item4);

        // Item 5: Epson Smart Laser Projector (David)
        Item item5 = Item.builder()
                .owner(david)
                .category(electronics)
                .subCategory("Home Theater Projectors")
                .title("Epson EpiqVision Mini EF-12 Portable Smart Laser Projector")
                .description("Stunning Full HD 1080p picture up to 150 inches with advanced 3-chip 3LCD laser technology. Built-in Android TV and custom Yamaha sound system. HDMI, USB, and wireless casting.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(750.0))
                .depositAmount(BigDecimal.valueOf(4000.0))
                .estimatedValue(BigDecimal.valueOf(75000.0))
                .minBorrowDays(1)
                .maxBorrowDays(4)
                .borrowingRules("Only use the provided padded case for transit. Do not touch optical lens surface.")
                .location("Whitefield Main Road, Bangalore")
                .borrowCount(9)
                .viewCount(165)
                .build();
        items.add(item5);

        // Item 6: Yamaha Acoustic Guitar (David)
        Item item6 = Item.builder()
                .owner(david)
                .category(music)
                .subCategory("Acoustic Guitars")
                .title("Yamaha F310 Full-Size Dreadnought Acoustic Guitar")
                .description("Rich, warm tone with spruce top and rosewood fingerboard. Perfect for recording, practice, or campfire gatherings. Includes padded gig bag, clip-on tuner, capo, and picks.")
                .condition(ItemCondition.GOOD)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(250.0))
                .depositAmount(BigDecimal.valueOf(1500.0))
                .estimatedValue(BigDecimal.valueOf(11000.0))
                .minBorrowDays(2)
                .maxBorrowDays(14)
                .borrowingRules("Keep inside gig bag when not playing. Do not expose to direct heat or moisture.")
                .location("Whitefield ITPL, Bangalore")
                .borrowCount(7)
                .viewCount(120)
                .build();
        items.add(item6);

        // Item 7: DJI Mini 3 Drone (Sarah)
        Item item7 = Item.builder()
                .owner(sarah)
                .category(cameras)
                .subCategory("Drones & Action Cams")
                .title("DJI Mini 3 Pro Drone with Smart Screen Remote (Fly More Combo)")
                .description("Under 249g lightweight 4K HDR drone with tri-directional obstacle sensing, true vertical shooting, and 34-minute flight time. Includes 3 intelligent flight batteries, two-way charging hub, shoulder bag, and ND filter set.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(1200.0))
                .depositAmount(BigDecimal.valueOf(8000.0))
                .estimatedValue(BigDecimal.valueOf(88000.0))
                .minBorrowDays(1)
                .maxBorrowDays(5)
                .borrowingRules("Only fly in open airspace complying with civil aviation regulations. Calibrate compass before takeoff.")
                .location("Indiranagar, Bangalore")
                .borrowCount(18)
                .viewCount(390)
                .build();
        items.add(item7);

        // Item 8: Bosch Circular Saw (Alex)
        Item item8 = Item.builder()
                .owner(alex)
                .category(tools)
                .subCategory("Saws & Cutters")
                .title("Bosch Professional GKS 190 Heavy Duty Circular Saw (1400W)")
                .description("Powerful 1400W motor with 70mm cutting depth and 56° bevel capacity. Turboblower for a dust-free view of the cutting line. Includes carbide-tipped wood blade, parallel guide, and safety goggles.")
                .condition(ItemCondition.GOOD)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(350.0))
                .depositAmount(BigDecimal.valueOf(2000.0))
                .estimatedValue(BigDecimal.valueOf(9500.0))
                .minBorrowDays(1)
                .maxBorrowDays(4)
                .borrowingRules("Always wear protective eye gear. Check workpiece for nails or metal before cutting.")
                .location("Koramangala, Bangalore")
                .borrowCount(13)
                .viewCount(145)
                .build();
        items.add(item8);

        // Item 9: Trekking Sleeping Bag (Priya)
        Item item9 = Item.builder()
                .owner(priya)
                .category(camping)
                .subCategory("Sleeping Bags & Mats")
                .title("Forclaz Trek 900 Ultralight Down Sleeping Bag (0°C Comfort)")
                .description("800 cuin ethical duck down fill for warm and ultra-compact sleeping on high-altitude treks. Mummy shape with ergonomic hood and compression dry bag.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(200.0))
                .depositAmount(BigDecimal.valueOf(1200.0))
                .estimatedValue(BigDecimal.valueOf(7000.0))
                .minBorrowDays(2)
                .maxBorrowDays(10)
                .borrowingRules("Always sleep with the washable cotton inner liner provided. Air out after trek.")
                .location("HSR Layout, Bangalore")
                .borrowCount(8)
                .viewCount(110)
                .build();
        items.add(item9);

        // Item 10: Bose S1 Pro PA Speaker (David)
        Item item10 = Item.builder()
                .owner(david)
                .category(electronics)
                .subCategory("PA Sound Systems")
                .title("Bose S1 Pro+ All-in-One Multi-Position PA System with Bluetooth")
                .description("Pro sound for events, outdoor acoustics, parties, and conferences. Built-in 3-channel mixer with ToneMatch processing, OLED displays, and rechargeable battery with up to 11 hours of playtime.")
                .condition(ItemCondition.NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("RATE_AND_DEPOSIT")
                .dailyRate(BigDecimal.valueOf(900.0))
                .depositAmount(BigDecimal.valueOf(6000.0))
                .estimatedValue(BigDecimal.valueOf(65000.0))
                .minBorrowDays(1)
                .maxBorrowDays(3)
                .borrowingRules("Do not expose to rain or drinks. Transport in the padded backpack case.")
                .location("Whitefield, Bangalore")
                .borrowCount(10)
                .viewCount(210)
                .build();
        items.add(item10);

        // Item 11: Stihl Hedge Trimmer (Alex)
        Item item11 = Item.builder()
                .owner(alex)
                .category(garden)
                .subCategory("Hedge Trimmers")
                .title("Stihl Gas Hedge Trimmer with 24-Inch Dual-Reciprocating Blade")
                .description("Lightweight 2-stroke engine with high blade speed for clean, sculpted garden hedges and thick bushes. Anti-vibration system and rotating multi-function handle.")
                .condition(ItemCondition.GOOD)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(400.0))
                .depositAmount(BigDecimal.valueOf(2000.0))
                .estimatedValue(BigDecimal.valueOf(16000.0))
                .minBorrowDays(1)
                .maxBorrowDays(3)
                .borrowingRules("Use 50:1 pre-mixed fuel provided. Wear heavy duty gloves.")
                .location("Koramangala, Bangalore")
                .borrowCount(6)
                .viewCount(95)
                .build();
        items.add(item11);

        // Item 12: Sony 50mm Prime Lens (Sarah)
        Item item12 = Item.builder()
                .owner(sarah)
                .category(cameras)
                .subCategory("Prime & Zoom Lenses")
                .title("Sony FE 50mm f/1.8 Prime Portrait Lens (E-Mount)")
                .description("Compact, lightweight prime lens with fast F1.8 aperture for creamy background bokeh and superb low-light portraiture. Includes front and rear caps, hood, and UV filter.")
                .condition(ItemCondition.LIKE_NEW)
                .status(ItemStatus.AVAILABLE)
                .lendingMode("DAILY_RATE")
                .dailyRate(BigDecimal.valueOf(250.0))
                .depositAmount(BigDecimal.valueOf(1500.0))
                .estimatedValue(BigDecimal.valueOf(18000.0))
                .minBorrowDays(1)
                .maxBorrowDays(7)
                .borrowingRules("Never remove UV protector. Keep lens caps on when swapping lenses.")
                .location("Indiranagar, Bangalore")
                .borrowCount(15)
                .viewCount(200)
                .build();
        items.add(item12);

        itemRepository.saveAll(items);
        log.info("Seeded 12 verified gear listings across power tools, cameras, camping, and audio.");

        // 4. Seed Item Images
        List<ItemImage> images = List.of(
                ItemImage.builder().item(item1).imageUrl("https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item1).imageUrl("https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800").isPrimary(false).displayOrder(1).build(),
                ItemImage.builder().item(item2).imageUrl("https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item3).imageUrl("https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item4).imageUrl("https://images.unsplash.com/photo-1581578731548-c64695cc6952?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item5).imageUrl("https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item6).imageUrl("https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item7).imageUrl("https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item8).imageUrl("https://images.unsplash.com/photo-1572981779307-38b8cabb2407?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item9).imageUrl("https://images.unsplash.com/photo-1526772662000-3f88f10405ff?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item10).imageUrl("https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item11).imageUrl("https://images.unsplash.com/photo-1617103996702-96ff29b1c467?w=800").isPrimary(true).displayOrder(0).build(),
                ItemImage.builder().item(item12).imageUrl("https://images.unsplash.com/photo-1617005082133-548c4dd27f35?w=800").isPrimary(true).displayOrder(0).build()
        );
        itemImageRepository.saveAll(images);

        // 5. Seed Realistic Borrow Requests & Transactions
        LocalDate today = LocalDate.now();

        // Scenario 1: READY_FOR_PICKUP (Alex borrowing Sony A7 III from Sarah)
        BorrowRequest req1 = BorrowRequest.builder()
                .item(item1)
                .borrower(alex)
                .owner(sarah)
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(4))
                .status(RequestStatus.ACCEPTED)
                .message("Hi Sarah, I need the A7 III for a landscape photography weekend in Coorg. Will take utmost care.")
                .purpose("Landscape photography weekend")
                .responseMessage("Accepted! Look forward to meeting you tomorrow for pickup.")
                .build();
        borrowRequestRepository.save(req1);

        BorrowTransaction tx1 = BorrowTransaction.builder()
                .borrowRequest(req1)
                .item(item1)
                .borrower(alex)
                .owner(sarah)
                .startDate(today.plusDays(1))
                .endDate(today.plusDays(4))
                .pickupCode("482910")
                .returnCode("918234")
                .depositHeld(BigDecimal.valueOf(5000.0))
                .status(TransactionStatus.READY_FOR_PICKUP)
                .handoverLocation("Indiranagar Starbucks, Bangalore")
                .notes("Pickup scheduled for 10:00 AM.")
                .build();
        borrowTransactionRepository.save(tx1);

        // Scenario 2: BORROWED (Priya borrowing DeWalt Drill from Sarah)
        BorrowRequest req2 = BorrowRequest.builder()
                .item(item2)
                .borrower(priya)
                .owner(sarah)
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(2))
                .status(RequestStatus.ACCEPTED)
                .message("Need the hammer drill to mount bookshelves in my new apartment.")
                .purpose("Mounting bookshelves")
                .responseMessage("Approved! Pickup anytime after 6 PM.")
                .build();
        borrowRequestRepository.save(req2);

        BorrowTransaction tx2 = BorrowTransaction.builder()
                .borrowRequest(req2)
                .item(item2)
                .borrower(priya)
                .owner(sarah)
                .startDate(today.minusDays(1))
                .endDate(today.plusDays(2))
                .pickupCode("319402")
                .returnCode("882149")
                .depositHeld(BigDecimal.valueOf(1500.0))
                .status(TransactionStatus.BORROWED)
                .ownerPickupConfirmed(true)
                .borrowerPickupConfirmed(true)
                .pickupTime(LocalDateTime.now().minusDays(1))
                .handoverLocation("Indiranagar 12th Main, Bangalore")
                .build();
        borrowTransactionRepository.save(tx2);

        // Condition log for Tx 2
        TransactionCondition cond1 = TransactionCondition.builder()
                .transaction(tx2)
                .recordedBy(sarah)
                .stage(ConditionStage.PICKUP)
                .condition(ItemCondition.GOOD)
                .notes("Drill tested and verified working. Two 4.0Ah batteries fully charged, bit set complete.")
                .photoUrls("https://images.unsplash.com/photo-1504148455328-c376907d081c?w=800")
                .build();
        transactionConditionRepository.save(cond1);

        // Scenario 3: COMPLETED (Alex borrowed Coleman Tent from Priya)
        BorrowRequest req3 = BorrowRequest.builder()
                .item(item3)
                .borrower(alex)
                .owner(priya)
                .startDate(today.minusDays(7))
                .endDate(today.minusDays(5))
                .status(RequestStatus.ACCEPTED)
                .message("Taking family for camping at Ramanagara.")
                .purpose("Family weekend camp")
                .build();
        borrowRequestRepository.save(req3);

        BorrowTransaction tx3 = BorrowTransaction.builder()
                .borrowRequest(req3)
                .item(item3)
                .borrower(alex)
                .owner(priya)
                .startDate(today.minusDays(7))
                .endDate(today.minusDays(5))
                .pickupCode("726194")
                .returnCode("553019")
                .depositHeld(BigDecimal.valueOf(2000.0))
                .status(TransactionStatus.COMPLETED)
                .ownerPickupConfirmed(true)
                .borrowerPickupConfirmed(true)
                .ownerReturnConfirmed(true)
                .borrowerReturnConfirmed(true)
                .pickupTime(LocalDateTime.now().minusDays(7))
                .returnTime(LocalDateTime.now().minusDays(5))
                .build();
        borrowTransactionRepository.save(tx3);

        // Rating for Tx 3 (Alex rated Priya 5★)
        Rating r1 = Rating.builder()
                .transaction(tx3)
                .fromUser(alex)
                .toUser(priya)
                .overallRating(5)
                .communicationRating(5)
                .punctualityRating(5)
                .reliabilityRating(5)
                .conditionRating(5)
                .comment("The Coleman tent was super clean, easy to pitch, and Priya explained all the accessories thoroughly. 10/10 experience!")
                .role("BORROWER_TO_OWNER")
                .build();
        ratingRepository.save(r1);

        // Scenario 4: Pending Request for Projector
        BorrowRequest req4 = BorrowRequest.builder()
                .item(item5)
                .borrower(priya)
                .owner(david)
                .startDate(today.plusDays(3))
                .endDate(today.plusDays(5))
                .status(RequestStatus.PENDING)
                .message("Planning a movie night with colleagues on the terrace. Is the Epson projector available?")
                .purpose("Terrace movie night")
                .build();
        borrowRequestRepository.save(req4);

        // Scenario 5: Dispute on Bose Speaker
        BorrowRequest req5 = BorrowRequest.builder()
                .item(item10)
                .borrower(david)
                .owner(sarah)
                .startDate(today.minusDays(4))
                .endDate(today.minusDays(2))
                .status(RequestStatus.ACCEPTED)
                .message("Need high quality portable PA for an acoustic open-mic.")
                .purpose("Acoustic open-mic session")
                .build();
        borrowRequestRepository.save(req5);

        BorrowTransaction tx5 = BorrowTransaction.builder()
                .borrowRequest(req5)
                .item(item10)
                .borrower(david)
                .owner(sarah)
                .startDate(today.minusDays(4))
                .endDate(today.minusDays(2))
                .pickupCode("109384")
                .returnCode("664820")
                .depositHeld(BigDecimal.valueOf(6000.0))
                .status(TransactionStatus.DISPUTED)
                .ownerPickupConfirmed(true)
                .borrowerPickupConfirmed(true)
                .pickupTime(LocalDateTime.now().minusDays(4))
                .build();
        borrowTransactionRepository.save(tx5);

        Dispute dispute1 = Dispute.builder()
                .transaction(tx5)
                .createdBy(sarah)
                .againstUser(david)
                .reason("ITEM_DAMAGED")
                .description("Upon return inspection, deep scratches were found on the top grill and volume dial knob is cracked. Requesting deposit deduction for repair.")
                .status(DisputeStatus.OPEN)
                .build();
        disputeRepository.save(dispute1);

        // 6. Seed Real-Time Chat Conversation
        Conversation conv1 = Conversation.builder()
                .participant1(alex)
                .participant2(sarah)
                .borrowRequest(req1)
                .transaction(tx1)
                .lastMessage("Great! I will bring both batteries fully charged and the 64GB card.")
                .lastMessageAt(LocalDateTime.now().minusHours(2))
                .build();
        conversationRepository.save(conv1);

        Message m1 = Message.builder()
                .conversation(conv1)
                .sender(alex)
                .recipient(sarah)
                .content("Hi Sarah, looking forward to picking up the Sony A7 III tomorrow morning!")
                .isRead(true)
                .readAt(LocalDateTime.now().minusHours(3))
                .build();

        Message m2 = Message.builder()
                .conversation(conv1)
                .sender(sarah)
                .recipient(alex)
                .content("Great! I will bring both batteries fully charged and the 64GB card.")
                .isRead(false)
                .build();
        messageRepository.saveAll(List.of(m1, m2));

        // 7. Seed In-App Notifications
        Notification notif1 = Notification.builder()
                .recipient(alex)
                .type(NotificationType.REQUEST_ACCEPTED)
                .title("Borrow Request Accepted!")
                .message("Sarah Jenkins accepted your request for Sony Alpha A7 III. Pickup code ready.")
                .linkUrl("/borrows")
                .referenceId(tx1.getId())
                .isRead(false)
                .build();

        Notification notif2 = Notification.builder()
                .recipient(sarah)
                .type(NotificationType.TRANSACTION_CREATED)
                .title("Booking Confirmed")
                .message("Transaction #1 with Alex Morgan is scheduled for tomorrow.")
                .linkUrl("/lends")
                .referenceId(tx1.getId())
                .isRead(true)
                .build();

        Notification notif3 = Notification.builder()
                .recipient(priya)
                .type(NotificationType.RATING_RECEIVED)
                .title("New 5-Star Review!")
                .message("Alex Morgan left you a 5-star review for Coleman Sundome 4-Person Tent.")
                .linkUrl("/profile")
                .referenceId(r1.getId())
                .isRead(false)
                .build();

        notificationRepository.saveAll(List.of(notif1, notif2, notif3));

        log.info("============================================================");
        log.info("BORROWBOX SEED DATA ENGINE INITIALIZED SUCCESSFULLY!");
        log.info("Users: 5 | Categories: 6 | Listings: 12 | Bookings: 4 | Disputes: 1");
        log.info("============================================================");
    }
}
