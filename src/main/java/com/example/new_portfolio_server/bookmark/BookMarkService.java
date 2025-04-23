package com.example.new_portfolio_server.bookmark;

import com.example.new_portfolio_server.board.PortfolioRepository;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.bookmark.dto.CreateBookMarkDto;
import com.example.new_portfolio_server.bookmark.dto.ResponseBookmarkDto;
import com.example.new_portfolio_server.bookmark.dto.UpdateBookMarkDto;
import com.example.new_portfolio_server.bookmark.entity.BookMark;
import com.example.new_portfolio_server.common.exception.DuplicateResourceException;
import com.example.new_portfolio_server.user.entity.User;
import com.example.new_portfolio_server.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookMarkService {
    private final BookMarkRepository bookMarkRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true) // 데이터 조회만을 위해서 readOnly = true
    public List<ResponseBookmarkDto> getBookMarks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DuplicateResourceException("해당 사용자가 존재하지 않습니다."));
        List<BookMark> bookMarks = user.getBookMarks();

        // 포트폴리오 정보 포함해서 반환
        return bookMarks.stream()
                .map(bookMark -> ResponseBookmarkDto.builder()
                        .id(bookMark.getId())
                        .enabled(bookMark.isEnabled())
                        .sort(bookMark.getSort())
                        .portfolio(bookMark.getPortfolio())
                        .build())
                .toList();
    }

    @Transactional
    public Long createBookMark(CreateBookMarkDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DuplicateResourceException("해당 사용자가 존재하지 않습니다."));

        Portfolio board = portfolioRepository.findById(dto.getPortfolioId())
                .orElseThrow(() -> new  DuplicateResourceException("해당 포트폴리오가 존재하지 않습니다."));

        // 이미 북마크가 존재하는지 확인
        if (bookMarkRepository.existsByUserAndPortfolio(user, board)) {
            throw new DuplicateResourceException("이미 북마크 된 게시글입니다.");
        }

        BookMark bookMark = dto.toEntity(user, board);
        bookMark.setUser(user);
        user.addBookMark(bookMark);

        // 포트폴리오에 북마크 추가
        board.getBookMarks().add(bookMark);

        return bookMarkRepository.save(bookMark).getId();
    }

    @Transactional
    public void updateBookMark(Long id, UpdateBookMarkDto dto) {
        BookMark bookMark = bookMarkRepository.findById(id)
                .orElseThrow(() -> new DuplicateResourceException("해당 북마크가 존재하지 않흡니다."));

        // enabled가 false인 경우 북마크 삭제
        if (dto.getEnabled() != null && !dto.getEnabled()) {
            bookMark.getUser().removeBookMark(bookMark);
            bookMarkRepository.delete(bookMark);
        } else {
            dto.applyTo(bookMark);
        }
    }
}
