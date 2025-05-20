package com.example.new_portfolio_server.comments;

import com.example.new_portfolio_server.board.PortfolioRepository;
import com.example.new_portfolio_server.board.entity.Portfolio;
import com.example.new_portfolio_server.comments.dto.CommentEditRequestDto;
import com.example.new_portfolio_server.comments.dto.CommentRequestDto;
import com.example.new_portfolio_server.comments.dto.CommentResponseDto;
import com.example.new_portfolio_server.comments.entity.Comments;
import com.example.new_portfolio_server.user.UserRepository;
import com.example.new_portfolio_server.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PortfolioRepository portfolioRepository;
    private final UserRepository userRepository;

    // 댓글 생성
    @Transactional
    public CommentResponseDto createComment(CommentRequestDto commentRequestDto){
        String username = commentRequestDto.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 유저입니다."));

        Long portfolioId = commentRequestDto.getPortfolioId();
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 포트폴리오입니다."));

        Comments comments = commentRequestDto.toEntity(portfolio, user);

        Comments saved = commentRepository.save(comments);
        return CommentResponseDto.fromEntity(saved);
    }

    // 댓글 수정
    @Transactional
    public CommentResponseDto updateComment(Long id, CommentEditRequestDto commentEditRequestDto){
        Comments comments = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 댓글입니다."));

        comments.setBody(commentEditRequestDto.getBody());
        commentRepository.save(comments);

        return CommentResponseDto.fromEntity(comments);
    }

    // 댓글 전체 조회
    public List<Comments> getCommentAll(){
        return commentRepository.findAll();
    }

    // 댓글 조회
    public CommentResponseDto getComment(Long id){
        Comments comments = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 댓글입니다."));

        return CommentResponseDto.fromEntity(comments);
    }

    // 포트폴리오에 달린 댓글 조회
    public List<CommentResponseDto> getCommentByPortfolio(Long portfolioId){
        List<Comments> comments = commentRepository.findAllByPortfolioId(portfolioId);
        return comments.stream().map(CommentResponseDto::fromEntity).toList();
    }

    // 댓글 삭제
    public void deleteComment(Long id){
        commentRepository.deleteById(id);
    }
}
