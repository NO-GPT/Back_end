package com.example.new_portfolio_server.board.likes;

import com.example.new_portfolio_server.board.dto.ResponseBoardDto;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.board.likes.dto.LikeResponse;
import com.example.new_portfolio_server.board.likes.entity.Like;
import com.example.new_portfolio_server.board.likes.repository.LikeRepository;
import com.example.new_portfolio_server.board.repsoitory.PortfolioRepository;
import com.example.new_portfolio_server.user.UserRepository;
import com.example.new_portfolio_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;


    // 좋아요 기능
    @Transactional
    public LikeResponse goodPortfolio(Long portfolioId, Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));

        boolean exists = likeRepository.existsByUserAndPortfolio(user, portfolio);

        if(exists){
            likeRepository.deleteByUserAndPortfolio(user, portfolio);
            portfolio.unlike();
            return LikeResponse.builder()
                    .userId(user.getId())
                    .portfolioId(portfolio.getId())
                    .message("좋아요 취소")
                    .build();
        }

        Like savedLike = likeRepository.save(
                Like.builder()
                        .user(user)
                        .portfolio(portfolio)
                        .build()
        );
        portfolio.like();

        return LikeResponse.fromEntity(savedLike, "좋아요 성공");
    }

    // 각 유저가 좋아요를 표시한 포트폴리오를 조회
    @Transactional
    public List<ResponseBoardDto> getLikeUser(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        List<Like> likes = likeRepository.findByUserId(userId);

        return likes.stream()
                .map(like -> ResponseBoardDto.fromEntity(like.getPortfolio()))
                .collect(Collectors.toList());
    }
}
