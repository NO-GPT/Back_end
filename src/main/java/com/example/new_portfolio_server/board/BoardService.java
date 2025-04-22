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
    public static final String PORTFOLIO_WITH_USERS_COLLECTION = "portfolios_with_users";

    public void initializeCollections() {
        logger.info("Initializing Typesense collections");
        try {
            typesenseClient.collections(PORTFOLIO_COLLECTION).delete();
            logger.debug("Deleted existing portfolio collection");
        } catch (Exception e) {
            logger.debug("No existing portfolio collection to delete: {}", e.getMessage());
        }

        List<Field> portfolioFields = Arrays.asList(
                new Field().name("id").type("string"),
                new Field().name("introduce").type("string"),
                new Field().name("skills").type("string"),
                new Field().name("part").type("string"),
                new Field().name("created_at").type("int64")
        );

        CollectionSchema portfolioSchema = new CollectionSchema()
                .name(PORTFOLIO_COLLECTION)
                .fields(portfolioFields);

        try {
            typesenseClient.collections(PORTFOLIO_WITH_USERS_COLLECTION).delete();
            logger.debug("Deleted existing portfolio_with_users collection");
        } catch (Exception e) {
            logger.debug("No existing portfolio_with_users collection to delete: {}", e.getMessage());
        }

        List<Field> portfolioWithUsersFields = Arrays.asList(
                new Field().name("id").type("string"),
                new Field().name("introduce").type("string"),
                new Field().name("skills").type("string"),
                new Field().name("part").type("string"),
                new Field().name("username").type("string"),
                new Field().name("field").type("string"),
                new Field().name("full_name").type("string"),
                new Field().name("email").type("string"),
                new Field().name("github_id").type("string"),
                new Field().name("stack").type("string"),
                new Field().name("user_group").type("string"),
                new Field().name("created_at").type("int64")
        );

        CollectionSchema portfolioWithUsersSchema = new CollectionSchema()
                .name(PORTFOLIO_WITH_USERS_COLLECTION)
                .fields(portfolioWithUsersFields);

        try {
            typesenseClient.collections().create(portfolioSchema);
            typesenseClient.collections().create(portfolioWithUsersSchema);
            logger.info("Typesense collections created successfully");
            indexAllPortfolios();
            logger.info("All portfolios indexed successfully");
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
        }
        for (Portfolio portfolio : portfolios) {
            indexPortfolio(portfolio);
        }
    }

    public void indexPortfolio(Portfolio portfolio) throws Exception {
        Map<String, Object> portfolioDoc = new HashMap<>();
        portfolioDoc.put("id", portfolio.getId().toString());
        portfolioDoc.put("introduce", portfolio.getIntroduce());
        portfolioDoc.put("skills", portfolio.getSkills());
        portfolioDoc.put("part", portfolio.getPart());
        portfolioDoc.put("created_at", portfolio.getCreateDate().toEpochSecond(ZoneOffset.UTC));
        typesenseClient.collections(PORTFOLIO_COLLECTION).documents().upsert(portfolioDoc);

        User user = portfolio.getUserId();
        Map<String, Object> portfolioWithUserDoc = new HashMap<>();
        portfolioWithUserDoc.put("id", portfolio.getId().toString());
        portfolioWithUserDoc.put("introduce", portfolio.getIntroduce());
        portfolioWithUserDoc.put("skills", portfolio.getSkills());
        portfolioWithUserDoc.put("part", portfolio.getPart());
        portfolioWithUserDoc.put("username", user.getUsername());
        portfolioWithUserDoc.put("field", user.getField());
        portfolioWithUserDoc.put("full_name", user.getFullName());
        portfolioWithUserDoc.put("email", user.getEmail());
        portfolioWithUserDoc.put("github_id", user.getGithubId() != null ? user.getGithubId() : "");
        portfolioWithUserDoc.put("stack", user.getStack());
        portfolioWithUserDoc.put("user_group", user.getGroup());
        portfolioWithUserDoc.put("created_at", portfolio.getCreateDate().toEpochSecond(ZoneOffset.UTC));
        typesenseClient.collections(PORTFOLIO_WITH_USERS_COLLECTION).documents().upsert(portfolioWithUserDoc);

        logger.debug("Portfolio indexed successfully: ID={}", portfolio.getId());
    }

    public List<Map<String, Object>> searchPortfolios(String keyword) throws Exception {
        logger.debug("Searching portfolios with keyword: {}", keyword);
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQ(keyword);
        searchParameters.setQueryBy("introduce,skills,part");
        searchParameters.setNumTypos("2");

        try {
            typesenseClient.collections(PORTFOLIO_COLLECTION).retrieve();
            SearchResult searchResult = typesenseClient.collections(PORTFOLIO_COLLECTION).documents().search(searchParameters);
            List<Map<String, Object>> results = searchResult.getHits().stream()
                    .map(SearchResultHit::getDocument)
                    .collect(Collectors.toList());
            logger.debug("Found {} portfolio search results for keyword: {}", results.size(), keyword);
            return results;
        } catch (ObjectNotFound e) {
            logger.error("Portfolio collection not found for keyword '{}': {}", keyword, e.getMessage());
            throw new RuntimeException("Portfolio collection not found. Please ensure data is indexed.", e);
        } catch (Exception e) {
            logger.error("Failed to search portfolios: {}", e.getMessage(), e);
            throw new RuntimeException("Portfolio search failed: " + e.getMessage(), e);
        }
    }

    public List<Map<String, Object>> searchPortfoliosAndUsers(String keyword) throws Exception {
        logger.debug("Searching portfolios and users with keyword: {}", keyword);
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.setQ(keyword);
        searchParameters.setQueryBy("introduce,skills,part,username,field,full_name,email,github_id,stack,user_group");
        searchParameters.setNumTypos("2");

        try {
            typesenseClient.collections(PORTFOLIO_WITH_USERS_COLLECTION).retrieve();
            SearchResult searchResult = typesenseClient.collections(PORTFOLIO_WITH_USERS_COLLECTION).documents().search(searchParameters);
            List<Map<String, Object>> results = searchResult.getHits().stream()
                    .map(SearchResultHit::getDocument)
                    .collect(Collectors.toList());
            logger.debug("Found {} portfolio and user search results for keyword: {}", results.size(), keyword);
            return results;
        } catch (ObjectNotFound e) {
            logger.error("Portfolio with users collection not found for keyword '{}': {}", keyword, e.getMessage());
            throw new RuntimeException("Portfolio with users collection not found. Please ensure data is indexed.", e);
        } catch (Exception e) {
            logger.error("Failed to search portfolios and users: {}", e.getMessage(), e);
            throw new RuntimeException("Portfolio and user search failed: " + e.getMessage(), e);
        }
    }

    public void reindexAllPortfolios() {
        logger.info("Reindexing all portfolios");
        try {
            initializeCollections();
            logger.info("Reindexing completed successfully");
        } catch (IllegalStateException e) {
            logger.error("Reindexing failed: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            logger.error("Reindexing failed: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reindex portfolios: " + e.getMessage(), e);
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
        String name = uuid + "_" + Paths.get(file.getOriginalFilename())
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
