package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.service.CategoryService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String listCategories(Authentication authentication, Model model) {
        List<Category> categories = categoryService.findByUsername(authentication.getName());
        model.addAttribute("categories", categories);
        return "category/list";
    }

    @GetMapping("/categories/new")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new Category());
        return "category/form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nama kategori tidak boleh kosong");
            return "redirect:/categories/new";
        }

        if (categoryService.existsByNameAndUsername(category.getName().trim(), username)) {
            redirectAttributes.addFlashAttribute("error", "Nama kategori sudah ada");
            return "redirect:/categories/new";
        }

        category.setName(category.getName().trim());
        categoryService.save(category, username);
        redirectAttributes.addFlashAttribute("success", "Kategori berhasil ditambahkan!");
        return "redirect:/categories";
    }

    @GetMapping("/categories/{id}/edit")
    public String showEditForm(@PathVariable Long id, Authentication authentication, Model model) {
        return categoryService.findByIdAndUsername(id, authentication.getName())
                .map(category -> {
                    model.addAttribute("category", category);
                    return "category/form";
                })
                .orElse("redirect:/categories");
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id, @ModelAttribute Category category, Authentication authentication, RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        if (category.getName() == null || category.getName().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nama kategori tidak boleh kosong");
            return "redirect:/categories/" + id + "/edit";
        }

        Category existing = categoryService.findByIdAndUsername(id, username).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "Kategori tidak ditemukan");
            return "redirect:/categories";
        }

        if (categoryService.existsByNameAndUsernameExcludingId(category.getName().trim(), username, id)) {
            redirectAttributes.addFlashAttribute("error", "Nama kategori sudah ada");
            return "redirect:/categories/" + id + "/edit";
        }

        existing.setName(category.getName().trim());
        existing.setDescription(category.getDescription());
        categoryService.save(existing, username);
        redirectAttributes.addFlashAttribute("success", "Kategori berhasil diperbarui!");
        return "redirect:/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        categoryService.deleteByIdAndUsername(id, authentication.getName());
        redirectAttributes.addFlashAttribute("success", "Kategori berhasil dihapus!");
        return "redirect:/categories";
    }
}