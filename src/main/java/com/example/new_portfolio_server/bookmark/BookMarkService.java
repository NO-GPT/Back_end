package com.example.new_portfolio_server.bookmark;

import com.example.new_portfolio_server.board.PortfolioRepository;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.bookmark.Dto.CreateBookMarkDto;
import com.example.new_portfolio_server.bookmark.Dto.UpdateBookMarkDto;
import com.example.new_portfolio_server.common.Exception.DuplicateResourceException;
import com.example.new_portfolio_server.user.User;
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

    @Transactional(readOnly = true)
    public List<BookMark> getBookMarks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new DuplicateResourceException("해당 사용자가 존재하지 않습니다."));
        return user.getBookMarks();
    }

    @Transactional
    public Long createBookMark(CreateBookMarkDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new DuplicateResourceException("해당 사용자가 존재하지 않습니다."));

        Portfolio board = portfolioRepository.findById(dto.getPortfolioId())
                .orElseThrow(() -> new  DuplicateResourceException("해당 포트폴리오가 존재하지 않습니다."));

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
