package com.example.new_portfolio_server.portfolioCRUD;

import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class PortfolioService{
    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository){
        this.portfolioRepository = portfolioRepository;
    }

    public List<Portfolio> getAllPortfolios(){
        return portfolioRepository.findAll();
    }

    public Portfolio getPortfolioById(Long id){
        return portfolioRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));
    }

    public Portfolio createPortfolio(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }

    public Portfolio updatePortfolio(Long id, Portfolio updatePortfolio){
        Portfolio existingPortfolio = getPortfolioById(id);
        existingPortfolio.setUpdateTime(updatePortfolio.getUpdateTime());
        existingPortfolio.setProjectIntroduce(updatePortfolio.getProjectIntroduce());
        existingPortfolio.setProjectPart(updatePortfolio.getProjectPart());
        existingPortfolio.setProjectContent(updatePortfolio.getProjectContent());
        existingPortfolio.setProjectFilesPath(updatePortfolio.getProjectFilesPath());
        existingPortfolio.setProjectLinks(updatePortfolio.getProjectLinks());
        existingPortfolio.setProgramingSkills(updatePortfolio.getProgramingSkills());

        return portfolioRepository.save(existingPortfolio);
    }

    public void deletePortfolio(Long id){
        portfolioRepository.deleteById(id);
    }



}
