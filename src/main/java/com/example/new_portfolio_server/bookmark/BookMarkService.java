package com.example.new_portfolio_server.bookmark;

import com.example.new_portfolio_server.bookmark.Dto.CreateBookMarkDto;
import com.example.new_portfolio_server.bookmark.Dto.UpdateBookMarkDto;
import com.example.new_portfolio_server.common.DuplicateResourceException;
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

        BookMark bookMark = dto.toEntity(user);
        bookMark.setUser(user);
        user.addBookMark(bookMark);

        return bookMarkRepository.save(bookMark).getId();
    }

    @Transactional
    public void updateBookMark(Long id, UpdateBookMarkDto dto) {
        BookMark bookMark = bookMarkRepository.findById(id)
                .orElseThrow(() -> new DuplicateResourceException("해당 북마크가 존재하지 않흡니다."));

        // DTO의 메서드를 이용하여 엔티티 업데이트
        dto.applyTo(bookMark);
//        // DTO에 값이 존재하는 경우에만 업데이트
//        if (dto.getEnabled() != null) {
//            bookMark.setEnabled(dto.getEnabled());
//        }
//        if (dto.getSort() != null) {
//            bookMark.setSort(dto.getSort());
//        }
    }
}
