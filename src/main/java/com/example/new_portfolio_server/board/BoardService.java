package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.common.exception.DuplicateResourceException;
import com.example.new_portfolio_server.user.UserRepository;
import com.example.new_portfolio_server.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.typesense.api.Client;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.model.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final FileRepository fileRepository;
    @Getter
    private final Client typesenseClient;

    public static final String PORTFOLIO_COLLECTION = "portfolios";
    public static final String USERS_COLLECTION = "users";

    public void initializeCollections() {
        logger.info("Initializing Typesense collections");
        try {
            // 포트폴리오 컬렉션
            try {
                typesenseClient.collections(PORTFOLIO_COLLECTION).delete();
                logger.debug("Deleted existing portfolio collection");
            } catch (Exception e) {
                logger.debug("No existing portfolio collection to delete: {}", e.getMessage());
            }

            List<Field> portfolioFields = Arrays.asList(
                    new Field().name("id").type("string"),
                    new Field().name("introduce").type("string").infix(true),
                    new Field().name("part").type("string").infix(true),
                    new Field().name("content").type("string").infix(true),
                    new Field().name("links").type("string").infix(true),
                    new Field().name("skills").type("string").infix(true),
                    new Field().name("createDate").type("int64"),
                    new Field().name("updateDate").type("int64")
            );
            CollectionSchema portfolioSchema = new CollectionSchema()
                    .name(PORTFOLIO_COLLECTION)
                    .fields(portfolioFields);

            // 유저 컬렉션
            try {
                typesenseClient.collections(USERS_COLLECTION).delete();
                logger.debug("Deleted existing users collection");
            } catch (Exception e) {
                logger.debug("No existing users collection to delete: {}", e.getMessage());
            }

            List<Field> userFields = Arrays.asList(
                    new Field().name("id").type("string"),
                    new Field().name("username").type("string").infix(true),
                    new Field().name("email").type("string").infix(true),
                    new Field().name("fullName").type("string").infix(true),
                    new Field().name("field").type("string").infix(true),
                    new Field().name("group").type("string").infix(true),
                    new Field().name("stack").type("string").infix(true),
                    new Field().name("githubId").type("string").infix(true),
                    new Field().name("profile").type("string").optional(true).infix(true),
                    new Field().name("createdAt").type("int64"),
                    new Field().name("updatedAt").type("int64")
            );
            CollectionSchema userSchema = new CollectionSchema()
                    .name(USERS_COLLECTION)
                    .fields(userFields);

            typesenseClient.collections().create(portfolioSchema);
            typesenseClient.collections().create(userSchema);
            logger.info("Typesense collections created successfully");

            indexAllPortfolios();
            indexAllUsers();
            logger.info("All portfolios and users indexed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Typesense collections: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Typesense collections", e);
        }
    }

    private void indexAllPortfolios() throws Exception {
        List<Portfolio> portfolios = portfolioRepository.findAll();
        logger.debug("Indexing {} portfolios", portfolios.size());
        if (portfolios.isEmpty()) {
            logger.warn("No portfolios found in database to index");
            return;
        }
        for (Portfolio portfolio : portfolios) {
            try {
                indexPortfolio(portfolio);
            } catch (Exception e) {
                logger.error("Failed to index portfolio ID={}: {}", portfolio.getId(), e.getMessage());
            }
        }
    }

    private void indexAllUsers() throws Exception {
        List<User> users = userRepository.findAll();
        logger.debug("Indexing {} users", users.size());
        if (users.isEmpty()) {
            logger.warn("No users found in database to index");
            return;
        }
        for (User user : users) {
            try {
                indexUser(user);
            } catch (Exception e) {
                logger.error("Failed to index user ID={}: {}", user.getId(), e.getMessage());
            }
        }
    }

    public void indexPortfolio(Portfolio portfolio) throws Exception {
        if (portfolio == null) {
            logger.error("Portfolio is null, cannot index");
            throw new IllegalArgumentException("Portfolio cannot be null");
        }

        Map<String, Object> portfolioDoc = new HashMap<>();
        portfolioDoc.put("id", String.valueOf(portfolio.getId()));
        portfolioDoc.put("introduce", portfolio.getIntroduce() != null ? portfolio.getIntroduce() : "");
        portfolioDoc.put("part", portfolio.getPart() != null ? portfolio.getPart() : "");
        portfolioDoc.put("content", portfolio.getContent() != null ? portfolio.getContent() : "");
        portfolioDoc.put("links", portfolio.getLinks() != null ? portfolio.getLinks() : "");
        portfolioDoc.put("skills", portfolio.getSkills() != null ? portfolio.getSkills() : "");
        portfolioDoc.put("createDate", portfolio.getCreateDate() != null ? portfolio.getCreateDate().toEpochSecond(ZoneOffset.UTC) : 0);
        portfolioDoc.put("updateDate", portfolio.getUpdateDate() != null ? portfolio.getUpdateDate().toEpochSecond(ZoneOffset.UTC) : 0);
        logger.debug("Indexing portfolio document: {}", portfolioDoc);

        try {
            typesenseClient.collections(PORTFOLIO_COLLECTION).documents().upsert(portfolioDoc);
            logger.debug("Portfolio document indexed: ID={}", portfolio.getId());
        } catch (Exception e) {
            logger.error("Failed to index portfolio document ID={}: {}", portfolio.getId(), e.getMessage());
            throw new RuntimeException("Failed to index portfolio document: " + e.getMessage(), e);
        }
    }

    private void indexUser(User user) throws Exception {
        if (user == null) {
            logger.error("User is null, cannot index");
            throw new IllegalArgumentException("User cannot be null");
        }

        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("id", String.valueOf(user.getId()));
        userDoc.put("username", user.getUsername() != null ? user.getUsername() : "");
        userDoc.put("email", user.getEmail() != null ? user.getEmail() : "");
        userDoc.put("fullName", user.getFullName() != null ? user.getFullName() : "");
        userDoc.put("field", user.getField() != null ? user.getField() : "");
        userDoc.put("group", user.getGroup() != null ? user.getGroup() : "");
        userDoc.put("stack", user.getStack() != null ? user.getStack() : "");
        userDoc.put("githubId", user.getGithubId() != null ? user.getGithubId() : "");
        userDoc.put("profile", user.getProfile() != null ? user.getProfile() : null);
        userDoc.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toEpochSecond(ZoneOffset.UTC) : 0);
        userDoc.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) : 0);
        logger.debug("Indexing user document: {}", userDoc);

        try {
            typesenseClient.collections(USERS_COLLECTION).documents().upsert(userDoc);
            logger.debug("User document indexed: ID={}", user.getId());
        } catch (Exception e) {
            logger.error("Failed to index user document ID={}: {}", user.getId(), e.getMessage());
            throw new RuntimeException("Failed to index user document: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> searchPortfoliosAndUsers(String keyword) throws Exception {
        logger.debug("Searching portfolios and users with keyword: {}", keyword);
        List<Map<String, Object>> allResults = new ArrayList<>();

        // 포트폴리오 검색
        SearchParameters portfolioParams = new SearchParameters();
        portfolioParams.setQ(keyword.isEmpty() ? "*" : keyword);
        portfolioParams.setQueryBy("introduce,part,content,links,skills");
        portfolioParams.setNumTypos("2");
        portfolioParams.setPerPage(100);
        portfolioParams.setPage(1);
        portfolioParams.setPrefix("true");
        portfolioParams.setInfix("always");

        try {
            SearchResult portfolioResult = typesenseClient.collections(PORTFOLIO_COLLECTION).documents().search(portfolioParams);
            List<Map<String, Object>> portfolioResults = portfolioResult.getHits().stream()
                    .map(SearchResultHit::getDocument)
                    .map(doc -> {
                        Map<String, Object> portfolioDoc = new HashMap<>();
                        portfolioDoc.put("id", doc.get("id"));
                        portfolioDoc.put("introduce", doc.get("introduce"));
                        portfolioDoc.put("part", doc.get("part"));
                        portfolioDoc.put("content", doc.get("content"));
                        portfolioDoc.put("links", doc.get("links"));
                        portfolioDoc.put("skills", doc.get("skills"));
                        portfolioDoc.put("createDate", doc.get("createDate"));
                        portfolioDoc.put("updateDate", doc.get("updateDate"));
                        portfolioDoc.put("type", "portfolio");
                        return portfolioDoc;
                    })
                    .toList();
            allResults.addAll(portfolioResults);
            logger.info("Found {} portfolio search results for keyword: {}", portfolioResults.size(), keyword);
        } catch (ObjectNotFound e) {
            logger.warn("Portfolio collection not found for keyword '{}'", keyword);
        } catch (Exception e) {
            logger.error("Failed to search portfolios: {}", e.getMessage(), e);
        }

        // 유저 검색
        SearchParameters userParams = new SearchParameters();
        userParams.setQ(keyword.isEmpty() ? "*" : keyword);
        userParams.setQueryBy("username,email,fullName,field,group,stack,githubId");
        userParams.setNumTypos("2");
        userParams.setPerPage(100);
        userParams.setPage(1);
        userParams.setPrefix("true");
        userParams.setInfix("always");

        try {
            SearchResult userResult = typesenseClient.collections(USERS_COLLECTION).documents().search(userParams);
            List<Map<String, Object>> userResults = userResult.getHits().stream()
                    .map(SearchResultHit::getDocument)
                    .map(doc -> {
                        Map<String, Object> userDoc = new HashMap<>();
                        userDoc.put("id", doc.get("id"));
                        userDoc.put("username", doc.get("username"));
                        userDoc.put("email", doc.get("email"));
                        userDoc.put("fullName", doc.get("fullName"));
                        userDoc.put("field", doc.get("field"));
                        userDoc.put("group", doc.get("group"));
                        userDoc.put("stack", doc.get("stack"));
                        userDoc.put("githubId", doc.get("githubId"));
                        userDoc.put("profile", doc.get("profile"));
                        userDoc.put("createdAt", doc.get("createdAt"));
                        userDoc.put("updatedAt", doc.get("updatedAt"));
                        userDoc.put("type", "user");
                        return userDoc;
                    })
                    .toList();
            allResults.addAll(userResults);
            logger.info("Found {} user search results for keyword: {}", userResults.size(), keyword);
        } catch (ObjectNotFound e) {
            logger.warn("Users collection not found for keyword '{}'", keyword);
        } catch (Exception e) {
            logger.error("Failed to search users: {}", e.getMessage(), e);
        }

        // 최신순 정렬 (createDate 또는 createdAt 기준)
        allResults.sort((a, b) -> {
            Long aTime = a.containsKey("createDate") && a.get("createDate") instanceof Number ?
                    ((Number) a.get("createDate")).longValue() :
                    a.containsKey("createdAt") && a.get("createdAt") instanceof Number ?
                            ((Number) a.get("createdAt")).longValue() : 0L;
            Long bTime = b.containsKey("createDate") && b.get("createDate") instanceof Number ?
                    ((Number) b.get("createDate")).longValue() :
                    b.containsKey("createdAt") && b.get("createdAt") instanceof Number ?
                            ((Number) b.get("createdAt")).longValue() : 0L;
            logger.trace("Comparing timestamps: aTime={} (type={}), bTime={} (type={})",
                    aTime, a.get("createDate") != null ? a.get("createDate").getClass().getSimpleName() :
                            a.get("createdAt") != null ? a.get("createdAt").getClass().getSimpleName() : "null",
                    bTime, b.get("createDate") != null ? b.get("createDate").getClass().getSimpleName() :
                            b.get("createdAt") != null ? b.get("createdAt").getClass().getSimpleName() : "null");
            return bTime.compareTo(aTime);
        });

        logger.info("Total {} search results for keyword: {}", allResults.size(), keyword);
        return allResults;
    }

    public void reindexAllPortfolios() {
        logger.info("Reindexing all portfolios and users");
        try {
            initializeCollections();
            logger.info("Reindexing completed successfully");
        } catch (Exception e) {
            logger.error("Reindexing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reindex portfolios and users: " + e.getMessage(), e);
        }
    }

    @Transactional
    public Portfolio createPortfolio(BoardDto boardDto) throws IOException {
        // 유저 조회
        User user = userRepository.findById(boardDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID입니다."));

        // 포트폴리오 생성 및 유저 설정
        Portfolio portfolio = boardDto.toEntity();
        portfolio.setUserId(user);

        // 포트폴리오 저장
        Portfolio saved = portfolioRepository.save(portfolio);

        // 파일 저장
        if (boardDto.getFiles() != null && !boardDto.getFiles().isEmpty()) {
            for (MultipartFile file : boardDto.getFiles()) {
                fileRepository.save(toFileEntity(file, saved));
            }
        }

        return saved;
    }

    // 수정
    @Transactional
    public Portfolio updatePortfolio(Long id, UpdateBoardDto boardDto) {
        Portfolio existing = portfolioRepository.findById(id)
                .orElseThrow(() -> new DuplicateResourceException("포트폴리오가 존재하지 않습니다."));

        // DTO를 사용해 엔티티 업데이트
        boardDto.applyTo(existing);

        return portfolioRepository.save(existing);
    }

    // 전체 조회
    @Transactional
    public List<Portfolio> getAllPortfolio(){
        return portfolioRepository.findAll();
    }

    // 부분 조회
    @Transactional
    public Portfolio getPortfolioById(Long id){
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new DuplicateResourceException("포트폴리오가 존재하지 않습니다."));
    }

    @Transactional
    public Optional<File> getFile(Long id) {
        return fileRepository.findById(id);
    }

    // 삭제
    @Transactional
    public void delete(Long id){
        portfolioRepository.deleteById(id);
    }

    private File toFileEntity(MultipartFile file, Portfolio portfolio) throws IOException{
        String uuid = UUID.randomUUID().toString();
        String name = uuid + "_" + Paths.get(Objects.requireNonNull(file.getOriginalFilename()))
                .getFileName()
                .toString()
                .replaceAll("[^a-zA-Z0-9.\\-_]", "_"); // 경로 제거

        return File.builder()
                .fileName(name)
                .contentType(file.getContentType())
                .size(file.getSize())
                .data(file.getBytes())
                .portfolio(portfolio)
                .build();
    }
}
