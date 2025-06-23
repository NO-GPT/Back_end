package com.example.new_portfolio_server.app;


import com.example.new_portfolio_server.bookmark.dto.ResponseBookmarkDto;
import com.example.new_portfolio_server.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class controller {

    @GetMapping
    public String test(){
        return "Hello, World!";
    }
}
