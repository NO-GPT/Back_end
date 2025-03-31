package com.example.new_portfolio_server.portfolioCRUD;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/portfolio")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService){
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public List<Portfolio> getAllPortfolios(){
        return portfolioService.getAllPortfolios();
    }

    // 조회
    @GetMapping("/{id}")
    public ResponseEntity<Portfolio> getPortfolioById(@PathVariable Long id){
        return ResponseEntity.ok(portfolioService.getPortfolioById(id));
    }

    //게시
    @PostMapping
    public ResponseEntity<Portfolio> createPortfolio(@RequestBody Portfolio portfolio){
        return ResponseEntity.ok(portfolioService.createPortfolio(portfolio));
    }

    // 수정
    @PutMapping("/{id}")
    public ResponseEntity<Portfolio> updatePortfolio(@PathVariable Long id, @RequestBody Portfolio updatedPortfolio){
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, updatedPortfolio));
    }

    // 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolio(@PathVariable Long id){
        portfolioService.deletePortfolio(id);
        return ResponseEntity.noContent().build();
    }

}
