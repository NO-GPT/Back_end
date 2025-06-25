package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.dto.CursorResponse;
import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.dto.UpdateBoardDto;
import com.example.new_portfolio_server.board.entity.Banner;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.board.exception.PortfolioNotFoundException;
import com.example.new_portfolio_server.board.file.ImageService;
import com.example.new_portfolio_server.board.repsoitory.BannerRepository;
import com.example.new_portfolio_server.board.repsoitory.FileRepository;
import com.example.new_portfolio_server.board.repsoitory.PortfolioRepository;
import com.example.new_portfolio_server.common.exception.DuplicateResourceException;
import com.example.new_portfolio_server.common.response.ApiResponse;
import com.example.new_portfolio_server.user.UserRepository;
import com.example.new_portfolio_server.user.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.typesense.api.Client;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class BoardService {
    private static final Logger logger = LoggerFactory.getLogger(BoardService.class);

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final FileRepository fileRepository;
    private final BannerRepository bannerRepository;

    private final ImageService imageService;

    @Getter
    private final Client typesenseClient;

    public static final String PORTFOLIO_COLLECTION = "portfolios";
    public static final String USERS_COLLECTION = "users";

    /*
     * Typesense에 포트폴리오와 사용자 컬렉션을 생성하고, 데이터베이스의 모든 데이터를 인덱싱
     * */
    public void initializeCollections() {
        logger.info("Initializing Typesense collections");
        try {
            // 포트폴리오 컬렉션
            try {
                typesenseClient.collections(PORTFOLIO_COLLECTION).delete();
            } catch (Exception ignored) {}

            // 포트폴리오 컬렉션의 스키마를 정의 Field는 각 필드 이름
            List<Field> portfolioFields = Arrays.asList(
                    new Field().name("id").type("string"),
                    new Field().name("introduce").type("string").infix(true), // infix(true)는 부분검색 활성화
                    new Field().name("part").type("string").infix(true),
                    new Field().name("content").type("string").infix(true),
                    new Field().name("links").type("string").infix(true),
                    new Field().name("skills").type("string").infix(true),
                    new Field().name("bookmarkCount").type("int32"),
                    new Field().name("createDate").type("int64"),
                    new Field().name("updateDate").type("int64")
            );
            typesenseClient.collections().create(new CollectionSchema().name(PORTFOLIO_COLLECTION).fields(portfolioFields)); // 컬렉션을 생성

            // 유저 컬렉션
            try { // 기존 컬렉션이 남아 있으면 이전 스키마와 충돌하거나 잘못된 데이터 구조로 검색이 동작할 수 있기에 삭제
                typesenseClient.collections(USERS_COLLECTION).delete();
            } catch (Exception ignored) {}

            // 유저 컬렉션의 스키마를 정의 Field는 각 필드 이름
            List<Field> userFields = Arrays.asList(
                    new Field().name("id").type("string"),
                    new Field().name("username").type("string").infix(true),
                    new Field().name("email").type("string").infix(true),
                    new Field().name("fullName").type("string").infix(true),
                    new Field().name("field").type("string").infix(true),
                    new Field().name("group").type("string").infix(true),
                    new Field().name("stack").type("string").infix(true),
                    new Field().name("githubId").type("string").infix(true),
                    new Field().name("profile").type("string").optional(true).infix(true), // optional(true)로 필수가 아님
                    new Field().name("createdAt").type("int64"),
                    new Field().name("updatedAt").type("int64")
            );
            typesenseClient.collections().create(new CollectionSchema().name(USERS_COLLECTION).fields(userFields)); // 컬렉션을 생성

            // 데이터 인덱싱
            portfolioRepository.findAll().forEach(portfolio -> {
                try {
                    // 데이터베이스의 모든 포트폴리오를 조회하고, 각 포트폴리오를 indexPortfolio로 인덱싱
                    indexPortfolio(portfolio);
                } catch (Exception e) {
                    logger.error("Failed to index portfolio ID={}: {}", portfolio.getId(), e.getMessage());
                }
            });
            userRepository.findAll().forEach(user -> {
                try {
                    // 데이터베이스의 모든 유저를 조회하고, 각 유저를 indexUser로 인덱싱
                    indexUser(user);
                } catch (Exception e) {
                    logger.error("Failed to index user ID={}: {}", user.getId(), e.getMessage());
                }
            });
            logger.info("Typesense collections initialized and indexed");
        } catch (Exception e) {
            logger.error("Failed to initialize Typesense collections: {}", e.getMessage());
            throw new RuntimeException("Failed to initialize Typesense collections", e);
        }
    }

    /*
     * 단일 포트폴리오를 Typesense의 portfolios 컬렉션에 인덱싱
     * */
    public void indexPortfolio(Portfolio portfolio) throws Exception {
        if (portfolio == null) throw new IllegalArgumentException("Portfolio cannot be null");

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", String.valueOf(portfolio.getId()));
        doc.put("introduce", portfolio.getIntroduce() != null ? portfolio.getIntroduce() : "");
        doc.put("part", portfolio.getPart() != null ? portfolio.getPart() : "");
        doc.put("content", portfolio.getContent() != null ? portfolio.getContent() : "");
        doc.put("links", portfolio.getLinks() != null ? portfolio.getLinks() : "");
        doc.put("skills", portfolio.getSkills() != null ? portfolio.getSkills() : "");
        doc.put("bookmarkCount", portfolio.getBookMarks().size());
        doc.put("createDate", portfolio.getCreateDate() != null ? portfolio.getCreateDate().toEpochSecond(ZoneOffset.UTC) : 0);
        doc.put("updateDate", portfolio.getUpdateDate() != null ? portfolio.getUpdateDate().toEpochSecond(ZoneOffset.UTC) : 0);

        // 컬렉션에 문서를 추가하거나 업데이트 upsert는 기존 문서가 있으면 갱신, 없으면 생성
        typesenseClient.collections(PORTFOLIO_COLLECTION).documents().upsert(doc);
    }

    /*
     * 단일 사용자를 Typesense의 users 컬렉션에 인덱싱
     * */
    private void indexUser(User user) throws Exception {
        if (user == null) throw new IllegalArgumentException("User cannot be null");

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", String.valueOf(user.getId()));
        doc.put("username", user.getUsername() != null ? user.getUsername() : "");
        doc.put("email", user.getEmail() != null ? user.getEmail() : "");
        doc.put("fullName", user.getFullName() != null ? user.getFullName() : "");
        doc.put("field", user.getField() != null ? user.getField() : "");
        doc.put("group", user.getGroup() != null ? user.getGroup() : "");
        doc.put("stack", user.getStack() != null ? user.getStack() : "");
        doc.put("githubId", user.getGithubId() != null ? user.getGithubId() : "");
        doc.put("profile", user.getProfile());
        doc.put("createdAt", user.getCreatedAt() != null ? user.getCreatedAt().toEpochSecond(ZoneOffset.UTC) : 0);
        doc.put("updatedAt", user.getUpdatedAt() != null ? user.getUpdatedAt().toEpochSecond(ZoneOffset.UTC) : 0);

        // 컬렉션에 문서를 추가하거나 업데이트 upsert는 기존 문서가 있으면 갱신, 없으면 생성
        typesenseClient.collections(USERS_COLLECTION).documents().upsert(doc);
    }

    /*
     * 주어진 키워드로 포트폴리오와 사용자를 Typesense에서 검색하고, 페이지 단위로 정렬된 결과를 반환
     */
    public List<Map<String, Object>> searchPortfoliosAndUsers(String keyword, int page, int size) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();

        // 포트폴리오 검색
        SearchParameters portfolioParams = new SearchParameters()   // 포트폴리오 검색을 위한 Typesense 검색 파라미터 객체를 생성
                .q(keyword.isEmpty() ? "*" : keyword)               // 키워드가 비어 있으면 모든 문서(*)를 검색하고, 아니면 입력된 키워드를 사용
                .queryBy("introduce,part,content,links,skills")     // 해당 필드에서 키워드를 검색
                .numTypos("2") // 최대 2글자 오타를 허용
                .perPage(size) // 한 페이지에 반환할 결과 수를 size로 설정
                .page(page)    // 요청된 페이지 번호를 page로 설정
                .prefix("true")// 접두사 검색을 활성화 (키워드 앞부분 일치)
                .infix("always"); // 중간 문자열 검색을 활성화

        try {
            // 컬렉션에서 검색을 실행하고 결과를 저장
            SearchResult portfolioResult = typesenseClient.collections(PORTFOLIO_COLLECTION).documents().search(portfolioParams);

            // 검색 결과(hits)를 스트림으로 처리하여 results에 추가
            results.addAll(portfolioResult.getHits().stream()
                    .map(SearchResultHit::getDocument)
                    .map(doc -> {

                        // 문서의 필드를 새 Map에 추가
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
                    .toList()); // 스트림을 리스트로 변환하고 results에 추가
        } catch (ObjectNotFound ignored) {
            logger.warn("Portfolio collection not found for keyword '{}'", keyword);
        } catch (Exception e) {
            logger.error("Failed to search portfolios for keyword '{}': {}", keyword, e.getMessage());
        }

        // 유저 검색
        SearchParameters userParams = new SearchParameters()                    // 유저 검색을 위한 Typesense 검색 파라미터 객체를 생성
                .q(keyword.isEmpty() ? "*" : keyword)
                .queryBy("username,email,fullName,field,group,stack,githubId")  // 해당 필드에서 키워드를 검색
                .numTypos("2")
                .perPage(size)
                .page(page)
                .prefix("true")
                .infix("always");

        try {
            SearchResult userResult = typesenseClient.collections(USERS_COLLECTION).documents().search(userParams);
            results.addAll(userResult.getHits().stream()
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
                    .toList());
        } catch (ObjectNotFound ignored) {
            logger.warn("Users collection not found for keyword '{}'", keyword);
        } catch (Exception e) {
            logger.error("Failed to search users for keyword '{}': {}", keyword, e.getMessage());
        }

        // 최신순 정렬
        results.sort((a, b) -> Long.compare(
                b.containsKey("createDate") && b.get("createDate") instanceof Number ? ((Number) b.get("createDate")).longValue() :
                        b.containsKey("createdAt") && b.get("createdAt") instanceof Number ? ((Number) b.get("createdAt")).longValue() : 0L,
                a.containsKey("createDate") && a.get("createDate") instanceof Number ? ((Number) a.get("createDate")).longValue() :
                        a.containsKey("createdAt") && a.get("createdAt") instanceof Number ? ((Number) a.get("createdAt")).longValue() : 0L
        ));

        logger.info("Found {} search results for keyword '{}'", results.size(), keyword);
        return results;
    }

    // 포트폴리오 업로드
    @Transactional
    public ApiResponse<Portfolio> createPortfolio(BoardDto boardDto) throws IOException {
        // 유저 조회
        User user = userRepository.findById(boardDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 ID입니다."));

        // 포트폴리오 생성 및 유저 설정
        Portfolio portfolio = boardDto.toEntity();
        portfolio.setUser(user);

        // 포트폴리오 저장
        Portfolio saved = portfolioRepository.save(portfolio);

        // 업로드할 배너 파일
        MultipartFile banner = boardDto.getBanner();
        if (banner != null && !banner.isEmpty()) {
            String bannerUrls = imageService.uploadFile(banner);

            Banner bannerEntity = Banner.builder()
                    .bannerName(banner.getOriginalFilename())
                    .contentType(banner.getContentType())
                    .bannerUrl(bannerUrls)
                    .size(banner.getSize())
                    .createdDate(LocalDateTime.now())
                    .portfolio(saved)
                    .build();

            bannerRepository.save(bannerEntity);
        }

        // 업로드할 파일
        List<MultipartFile> files = boardDto.getFiles();

        if (files != null && !files.isEmpty()) {
            List<String> fileUrls = imageService.uploadFile(files);

            for (int i = 0; i < files.size(); i++) {
                MultipartFile multipartFile = files.get(i);
                String fileUrl = fileUrls.get(i);

                File file = File.builder()
                        .fileName(multipartFile.getOriginalFilename())
                        .contentType(multipartFile.getContentType())
                        .size(multipartFile.getSize())
                        .fileUrl(fileUrl)
                        .createdDate(LocalDateTime.now())
                        .portfolio(saved)
                        .build();

                fileRepository.save(file);
            }
        }
        return ApiResponse.success("포트폴리오 생성 성공", saved);
    }

    //수정
    @Transactional
    public Portfolio updatePortfolio(Long id, UpdateBoardDto boardDto, List<MultipartFile> newFiles, MultipartFile banner) {
        Portfolio existing = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));

        boardDto.applyTo(existing);

        // 기존 배너 삭제 및 새 배너 저장
        if (banner != null && !banner.isEmpty()) {
            List<Banner> existingBanners = existing.getBanner_file();
            for (Banner b : existingBanners) {
                imageService.deleteFile(b.getBannerUrl());
            }
            existingBanners.clear();

            String fileKey = imageService.uploadFile(banner);
            Banner newBanner = Banner.builder()
                    .bannerName(banner.getOriginalFilename())
                    .contentType(banner.getContentType())
                    .size(banner.getSize())
                    .bannerUrl(fileKey)
                    .createdDate(LocalDateTime.now())
                    .portfolio(existing)
                    .build();

            existingBanners.add(newBanner);
        }

        // 새 파일 추가
        if (newFiles != null && !newFiles.isEmpty()) {
            List<MultipartFile> validFiles = newFiles.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .toList();

            if (!validFiles.isEmpty()) {
                List<String> fileUrls = imageService.uploadFile(validFiles);

                for (int i = 0; i < validFiles.size(); i++) {
                    MultipartFile file = validFiles.get(i);
                    String url = fileUrls.get(i);

                    File fileEntity = File.builder()
                            .fileName(file.getOriginalFilename())
                            .fileUrl(url)
                            .contentType(file.getContentType())
                            .size(file.getSize())
                            .createdDate(LocalDateTime.now())
                            .portfolio(existing)
                            .build();

                    fileRepository.save(fileEntity);
                }
            }
        }

        return portfolioRepository.save(existing);
    }


    // 좋아요 개수를 기준으로 정렬된 커서 기반 페이지 네이션 - 포트폴리오
    public CursorResponse getAllPortfolioSortedByLike(Long likeCount, Long cursorId, int limit) {
        List<Portfolio> portfolios;

        if (likeCount == null || cursorId == null) {
            portfolios = portfolioRepository.findInitialPortfolios(limit);
        } else {
            portfolios = portfolioRepository.findPortfolioByCursor(likeCount, cursorId, limit);
        }

        if (portfolios.isEmpty()) {
            throw new PortfolioNotFoundException("포트폴리오가 더 이상 존재하지 않습니다.");
        }

        List<ResponseBoardDto> result = portfolios.stream()
                .map(ResponseBoardDto::fromEntity)
                .collect(Collectors.toList());

        Portfolio last = portfolios.getLast();
        return new CursorResponse(last.getLikeCount(), last.getId(), result);
    }

    @Transactional
    public List<ResponseBoardDto> getTopBookmarkedPortfolios(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        List<Portfolio> portfolios = portfolioRepository.findAllOrderByBookMarksSizeDesc(pageable);
        return toResponseBoardDtos(portfolios);
    }

    // 부분 조회
    @Transactional
    public ResponseBoardDto getPortfolioById(Long id){
        Portfolio board = portfolioRepository.findById(id)
                .orElseThrow(() -> new DuplicateResourceException("포트폴리오가 존재하지 않습니다."));

        return ResponseBoardDto.builder()
                .id(board.getId())
                .introduce(board.getIntroduce())
                .part(board.getPart())
                .content(board.getContent())
                .links(board.getLinks())
                .skills(board.getSkills())
                .createDate(board.getCreateDate())
                .updateDate(board.getUpdateDate())
                .userId(board.getUser().getId()) // 유저 ID 설정
                .username(board.getUser().getUsername())
                .banner(board.getBanner_file())
                .files(fileRepository.findByPortfolioId(id)) // 파일 목록 조회
                .bookmarkCount((long)board.getBookMarks().size()) // 북마크 개수 설정
                .bookMarks(board.getBookMarks())
                .comments(board.getComments())
                .build();
    }

    @Transactional
    public List<ResponseBoardDto> getPortfolioByUsername(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        List<Portfolio> portfolios = portfolioRepository.findByUserId_Username(username);

        return portfolios.stream().map(board -> ResponseBoardDto.builder()
                .id(board.getId())
                .introduce(board.getIntroduce())
                .part(board.getPart())
                .content(board.getContent())
                .links(board.getLinks())
                .skills(board.getSkills())
                .createDate(board.getCreateDate())
                .updateDate(board.getUpdateDate())
                .userId(board.getUser().getId())
                .username(board.getUser().getUsername())
                .banner(board.getBanner_file())
                .files(fileRepository.findByPortfolioId(board.getId()))
                .bookmarkCount((long)board.getBookMarks().size())
                .bookMarks(board.getBookMarks())
                .comments(board.getComments())
                .build()
        ).toList();
    }

    @Transactional
    public Optional<File> getFile(Long id) {
        return fileRepository.findById(id);
    }

//    // 카테고리 검색
//    @Transactional
//    public List<ResponseBoardDto> searchByCategorys(List<String> parts, List<String> groups, List<String> skills) {
//        List<Portfolio> portfolios;
//
//        if ((parts == null || parts.isEmpty()) &&
//                (groups == null || groups.isEmpty()) &&
//                (skills == null || skills.isEmpty())) {
//            portfolios = portfolioRepository.findAll();
//        }
//        else if ((parts != null && !parts.isEmpty()) && (groups != null && !groups.isEmpty())) {
//            portfolios = portfolioRepository.findByPartInAndUser_GroupIn(parts, groups);
//        }
//        else if (parts != null && !parts.isEmpty()) {
//            portfolios = portfolioRepository.findByPartIn(parts);
//        }
//        else if (groups != null && !groups.isEmpty()) {
//            portfolios = portfolioRepository.findByUser_GroupIn(groups);
//        } else {
//            portfolios = portfolioRepository.findAll();  // 기본 fallback
//        }
//
//        if (skills != null && !skills.isEmpty()) {
//            portfolios = portfolios.stream()
//                    .filter(p -> {
//                        String pSkills = p.getSkills();
//                        if(pSkills == null) return false;
//
//                        List<String> savedSkills = Arrays.stream(pSkills.split("[,\\s]+"))
//                                .map(String::toLowerCase)
//                                .toList();
//
//                        return skills.stream()
//                                .map(String::toLowerCase)
//                                .allMatch(savedSkills::contains);
//                    })
//                    .collect(Collectors.toList());
//        }
//
//        return portfolios.stream()
//                .map(ResponseBoardDto::fromEntity)
//                .collect(Collectors.toList());
//    }

    // 카테고리 분류 - 페이지네이션 적용
    @Transactional
    public CursorResponse searchByCategorysWithCursor(
            List<String> parts, List<String> groups, List<String> skills,
            Long likeCount, Long cursorId, int limit
    ){
        List<Portfolio> filtered;

        // 값존재 X
        if((parts == null || parts.isEmpty()) && (groups == null || groups.isEmpty())){
            filtered = portfolioRepository.findAll();
        }
        // part, group 존재할 시
        else if(parts != null && !parts.isEmpty() && groups != null && !groups.isEmpty()){
            filtered = portfolioRepository.findByPartInAndUser_GroupIn(parts, groups);
        }
        // parts만 존재할 시
        else if(parts != null && !parts.isEmpty()){
            filtered = portfolioRepository.findByPartIn(parts);
        }
        // groups 존재할 시, 기타 등
        else {
            filtered = portfolioRepository.findByUser_GroupIn(groups);
        }

        // skill 존재할 시
        if(skills != null && !skills.isEmpty()){
            filtered = filtered.stream()
                    .filter(p -> {
                        String pSkills = p.getSkills();
                        if(pSkills == null) return false;
                        List<String> savedSkills = Arrays.stream(pSkills.split("[,\\s]+"))
                                .map(String::toLowerCase)
                                .toList();
                        return skills.stream()
                                .map(String::toLowerCase)
                                .allMatch(savedSkills::contains);
                    })
                    .collect(Collectors.toList());
        }

        // 커서 기반 페이징 네이션(좋아요)
        List<Portfolio> sorted = filtered.stream()
                .sorted(Comparator
                        .comparing(Portfolio::getLikeCount, Comparator.reverseOrder())
                        .thenComparing(Portfolio::getId, Comparator.reverseOrder()))
                .toList();

        List<Portfolio> paged;
        if(likeCount == null || cursorId == null){
            paged = sorted.stream().limit(limit).toList();
        }
        else {
            paged = sorted.stream()
                    .dropWhile(p -> {
                        if(p.getLikeCount().equals(likeCount)){
                            return p.getId() >= cursorId;
                        }
                        return p.getLikeCount() > likeCount;
                    })
                    .limit(limit)
                    .toList();
        }

        if(paged.isEmpty()){
            throw new PortfolioNotFoundException("포트폴리오가 더 이상 존재하지 않습니다.");
        }

        List<ResponseBoardDto> result = paged.stream()
                .map(ResponseBoardDto::fromEntity)
                .collect(Collectors.toList());

        Portfolio last = paged.getLast();
        return new CursorResponse(last.getLikeCount(), last.getId(), result);
    }


    // 포폴 삭제
    @Transactional
    public void delete(Long id){
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));

        // 연관된 파일 이름 가져오기
        List<String> fileNames = portfolio.getFiles().stream()
                .map(File::getFileUrl)
                .toList();

        // S3에서 파일 삭제
        fileNames.forEach(imageService::deleteFile);

        List<String> bannerNames = portfolio.getBanner_file().stream()
                .map(Banner::getBannerUrl)
                .toList();

        bannerNames.forEach(imageService::deleteFile);
        portfolioRepository.deleteById(id);
    }


    private List<ResponseBoardDto> toResponseBoardDtos(List<Portfolio> portfolios) {
        return portfolios.stream()
                .map(board -> ResponseBoardDto.builder()
                        .id(board.getId())
                        .introduce(board.getIntroduce())
                        .part(board.getPart())
                        .content(board.getContent())
                        .links(board.getLinks())
                        .skills(board.getSkills())
                        .createDate(board.getCreateDate())
                        .updateDate(board.getUpdateDate())
                        .userId(board.getUser().getId())
                        .banner(bannerRepository.findByPortfolioId(board.getId()))
                        .files(fileRepository.findByPortfolioId(board.getId()))
                        .bookMarks(board.getBookMarks())
                        .bookmarkCount((long)board.getBookMarks().size())
                        .comments(board.getComments())
                        .build())
                .collect(Collectors.toList());
    }
}