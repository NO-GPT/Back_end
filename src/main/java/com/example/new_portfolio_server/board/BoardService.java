package com.example.new_portfolio_server.board;

import com.example.new_portfolio_server.board.dto.BoardDto;
import com.example.new_portfolio_server.board.entity.File;
import com.example.new_portfolio_server.board.entity.Portfolio;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Transactional //
public class BoardService {
    @Autowired
    private final PortfolioRepository portfolioRepository;
    private final FileRepository fileRepository;

    // 게시
    public Portfolio createPortfolio(BoardDto boardDto) throws IOException {
        Portfolio portfolio = Portfolio.builder()
                .createDate(boardDto.getCreateDate())
                .updateDate(boardDto.getUpdateDate())
                .introduce(boardDto.getIntroduce())
                .part(boardDto.getPart())
                .content(boardDto.getContent())
                .links(boardDto.getLinks())
                .skills(boardDto.getSkills())
                .build();

        Portfolio saved = portfolioRepository.save(portfolio);

        if(boardDto.getFiles() != null){
            for (MultipartFile file : boardDto.getFiles()){
//                validImage(file);
                fileRepository.save(toFileEntity(file, saved));
            }
        }
        return saved;
    }

    // 수정
    public Portfolio updatePortfolio(Long id, BoardDto boardDto) throws IOException{
        Portfolio existing = portfolioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("포트폴리오가 존재하지 않습니다."));

        existing.setUpdateDate(boardDto.getUpdateDate());
        existing.setIntroduce(boardDto.getIntroduce());
        existing.setPart(boardDto.getPart());
        existing.setContent(boardDto.getContent());
        existing.setLinks(boardDto.getLinks());
        existing.setSkills(boardDto.getSkills());

        fileRepository.deleteAll(fileRepository.findByPortfolioId(id));

        if(boardDto.getFiles() != null){
            for(MultipartFile file : boardDto.getFiles()){
                fileRepository.save(toFileEntity(file, existing));
            }
        }

        return portfolioRepository.save(existing);
    }

    // 전체 조회
    public List<Portfolio> getAllPortfolio(){
        return portfolioRepository.findAll();
    }

    // 부분 조회
    public Portfolio getPortfolioById(Long id){
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));
    }

    public Optional<File> getFile(Long id) {
        return fileRepository.findById(id);
    }

    // 삭제
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
