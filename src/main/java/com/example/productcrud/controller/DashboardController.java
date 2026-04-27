package com.example.productcrud.controller;

import com.example.productcrud.dto.DashboardStats;
import com.example.productcrud.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final ProductService productService;

    public DashboardController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        DashboardStats stats = productService.getDashboardStats(authentication.getName());
        model.addAttribute("stats", stats);
        return "dashboard";
    }
}