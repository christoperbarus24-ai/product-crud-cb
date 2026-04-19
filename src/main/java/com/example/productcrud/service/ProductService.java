package com.example.productcrud.service;

import com.example.productcrud.Repository.Productspecification;
import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import com.example.productcrud.Repository.Productrepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final Productrepository productRepository;

    // Jumlah produk per halaman: 5 sesuai instruksi tugas
    private static final int PAGE_SIZE = 5;

    public ProductService(Productrepository productRepository) {
        this.productRepository = productRepository;
    }

    // Dipakai oleh DashboardController yang butuh semua data tanpa pagination
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    /**
     * Mencari produk dengan dukungan:
     * - Keyword: partial match, case-insensitive pada nama produk
     * - Category: filter berdasarkan enum Category (null = semua kategori)
     * - Pagination: 5 produk per halaman, diurutkan berdasarkan id ascending
     *
     * Menggunakan Specification pattern (JPA Criteria API) untuk menghindari
     * masalah type casting PostgreSQL (lower(bytea) error) yang terjadi pada @Query JPQL.
     *
     * Fix: tidak menggunakan Specification.where() yang deprecated sejak Spring 3.5.0.
     * Sebagai gantinya, langsung menggunakan operator .and() pada Specification pertama,
     * sesuai dengan cara kerja Specification yang mengimplementasikan interface fungsional.
     *
     * @param keyword  kata kunci pencarian (boleh null/kosong)
     * @param category filter kategori (boleh null = semua)
     * @param page     nomor halaman (0-based dari Spring, tapi UI kirim 1-based)
     * @return Page<Product> berisi data produk dan info pagination
     */
    public Page<Product> searchAndFilter(String keyword, Category category, int page) {
        // Pageable: page 0-based, sort by id ASC agar urutan konsisten antar halaman
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").ascending());

        // Gabungkan dua Specification dengan AND tanpa Specification.where() yang deprecated.
        // nameContains dan categoryEquals masing-masing mengembalikan conjunction()
        // (kondisi selalu true) jika parameternya null/kosong,
        // sehingga query tetap benar untuk semua kombinasi input.
        Specification<Product> spec = Productspecification.nameContains(keyword)
                .and(Productspecification.categoryEquals(category));

        return productRepository.findAll(spec, pageable);
    }
}