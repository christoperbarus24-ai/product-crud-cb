package com.example.productcrud.service;

import com.example.productcrud.dto.DashboardStats;
import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.model.User;
import com.example.productcrud.repository.CategoryRepository;
import com.example.productcrud.repository.ProductRepository;
import com.example.productcrud.repository.ProductSpecification;
import com.example.productcrud.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final int PAGE_SIZE = 10;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public Page<Product> searchAndFilter(String username, String keyword, Long categoryId, int page) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

        Specification<Product> spec = ProductSpecification.withFilter(user.getId(), keyword, categoryId);

        return productRepository.findAll(spec, pageable);
    }

    public Optional<Product> findByIdAndUsername(Long id, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && product.get().getUserId().equals(user.getId())) {
            return product;
        }
        return Optional.empty();
    }

    public Product save(Product product, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        product.setUserId(user.getId());
        return productRepository.save(product);
    }

    public void deleteByIdAndUsername(Long id, String username) {
        Optional<Product> product = findByIdAndUsername(id, username);
        product.ifPresent(productRepository::delete);
    }

    public List<Category> findCategoriesByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        return categoryRepository.findByUserIdOrderByNameAsc(user.getId());
    }

    public Optional<Category> findCategoryByIdAndUsername(Long categoryId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));
        return categoryRepository.findByIdAndUserId(categoryId, user.getId());
    }

    public DashboardStats getDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan: " + username));

        List<Product> allProducts = productRepository.findByUserId(user.getId(), Pageable.unpaged()).getContent();

        DashboardStats stats = new DashboardStats();
        stats.setTotalProduk(allProducts.size());

        BigDecimal totalNilaiInventory = BigDecimal.ZERO;
        long jumlahAktif = 0;
        long jumlahTidakAktif = 0;
        Map<String, Long> produkPerKategori = new LinkedHashMap<>();
        List<Product> lowStockList = new ArrayList<>();

        for (Product p : allProducts) {
            totalNilaiInventory = totalNilaiInventory.add(
                    BigDecimal.valueOf(p.getPrice()).multiply(BigDecimal.valueOf(p.getStock()))
            );

            if (p.isActive()) {
                jumlahAktif++;
            } else {
                jumlahTidakAktif++;
            }

            if (p.getCategory() != null) {
                String catName = p.getCategory().getName();
                produkPerKategori.put(catName, produkPerKategori.getOrDefault(catName, 0L) + 1);
            }

            if (p.getStock() < 5) {
                lowStockList.add(p);
            }
        }

        stats.setTotalNilaiInventory(totalNilaiInventory);
        stats.setJumlahAktif(jumlahAktif);
        stats.setJumlahTidakAktif(jumlahTidakAktif);
        stats.setProdukPerKategori(produkPerKategori);
        stats.setLowStockList(lowStockList);

        return stats;
    }
}