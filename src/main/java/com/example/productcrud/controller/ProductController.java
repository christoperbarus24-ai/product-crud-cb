package com.example.productcrud.controller;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.service.ProductService;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
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

    /**
     * Menampilkan daftar produk dengan fitur:
     * - Search by keyword (partial match, case-insensitive)
     * - Filter by category (dropdown dari enum Category)
     * - Pagination 10 produk per halaman
     * - Semua parameter bisa digunakan bersamaan
     * - Parameter dikirim via query string: ?keyword=...&category=...&page=...
     */
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "category", required = false) String categoryStr,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            Model model) {

        // Konversi String category ke enum Category
        // Jika kosong/null/tidak valid -> null (artinya tampilkan semua kategori)
        Category category = null;
        if (categoryStr != null && !categoryStr.trim().isEmpty()) {
            try {
                category = Category.valueOf(categoryStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Kategori tidak valid, abaikan filter category
                category = null;
            }
        }

        // UI mengirim page 1-based, Spring Pageable butuh 0-based
        // Pastikan page tidak negatif
        int pageIndex = Math.max(0, page - 1);

        // Ambil data dari service: search + filter + pagination
        Page<Product> productPage = productService.searchAndFilter(keyword, category, pageIndex);

        // Kirim data ke view
        model.addAttribute("products", productPage.getContent());       // list produk halaman ini
        model.addAttribute("currentPage", productPage.getNumber() + 1); // 1-based untuk UI
        model.addAttribute("totalPages", productPage.getTotalPages());   // total halaman
        model.addAttribute("totalItems", productPage.getTotalElements());// total produk (semua halaman)
        model.addAttribute("pageSize", productPage.getSize());           // 10

        // Pertahankan nilai search/filter di form saat pindah halaman
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategory", categoryStr != null ? categoryStr : "");

        // Dropdown kategori dari enum
        model.addAttribute("categories", Category.values());

        return "product/list";
    }

    @GetMapping("/products/{id}")
    public String detailProduct(@PathVariable Long id, Model model) {
        return productService.findById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    return "product/detail";
                })
                .orElse("redirect:/products");
    }

    @GetMapping("/products/new")
    public String showCreateForm(Model model) {
        Product product = new Product();
        product.setCreatedAt(LocalDate.now());
        model.addAttribute("product", product);
        model.addAttribute("categories", Category.values());
        return "product/form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes redirectAttributes) {
        productService.save(product);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil disimpan!");
        return "redirect:/products";
    }

    @GetMapping("/products/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        return productService.findById(id)
                .map(product -> {
                    model.addAttribute("product", product);
                    model.addAttribute("categories", Category.values());
                    return "product/form";
                })
                .orElse("redirect:/products");
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Produk berhasil dihapus!");
        return "redirect:/products";
    }
}