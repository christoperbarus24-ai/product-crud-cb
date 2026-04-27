package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.service.ProductService;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/products";
    }

    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            Authentication authentication,
            Model model) {

        String username = authentication.getName();
        Page<Product> productPage = productService.searchAndFilter(username, keyword, categoryId, page - 1);

        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("totalItems", productPage.getTotalElements());
        model.addAttribute("pageSize", productPage.getSize());
        model.addAttribute("hasPrevious", productPage.hasPrevious());
        model.addAttribute("hasNext", productPage.hasNext());

        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);

        model.addAttribute("categories", productService.findCategoriesByUsername(username));

        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        return productService.findByIdAndUsername(id, username)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElse("redirect:/products");
    }

    @GetMapping("/products/new")
    public String showCreateForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        Product product = new Product();
        product.setCreatedAt(LocalDate.now());
        product.setActive(true);
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.findCategoriesByUsername(username));
        return "product/form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = productService.findCategoryByIdAndUsername(product.getCategory().getId(), username).orElse(null);
            product.setCategory(category);
        } else {
            product.setCategory(null);
        }

        productService.save(product, username);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil disimpan!");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();
        return productService.findByIdAndUsername(id, username)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("categories", productService.findCategoriesByUsername(username));
                    return "product/form";
                })
                .orElse("redirect:/products");
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        Product existing = productService.findByIdAndUsername(id, username).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Produk tidak ditemukan");
            return "redirect:/products";
        }

        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = productService.findCategoryByIdAndUsername(product.getCategory().getId(), username).orElse(null);
            existing.setCategory(category);
        } else {
            existing.setCategory(null);
        }

        existing.setName(product.getName());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setDescription(product.getDescription());
        existing.setActive(product.isActive());
        existing.setCreatedAt(product.getCreatedAt());

        productService.save(existing, username);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil diperbarui!");
        return "redirect:/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        productService.deleteByIdAndUsername(id, username);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        return "redirect:/products";
    }
}