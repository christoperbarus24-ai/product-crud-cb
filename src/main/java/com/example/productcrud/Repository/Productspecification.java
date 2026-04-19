package com.example.productcrud.Repository;

import com.example.productcrud.model.Category;
import com.example.productcrud.model.Product;
import org.springframework.data.jpa.domain.Specification;

/**
 * ProductSpecification menyediakan kondisi query dinamis menggunakan
 * JPA Criteria API (Specification pattern).
 *
 * Keunggulan vs @Query JPQL:
 * - Tidak ada masalah type casting dengan PostgreSQL (lower(bytea) error)
 * - Kondisi null ditangani di Java sebelum query dikirim ke DB
 * - Mudah dikombinasikan dengan Specification.where().and()
 */
public class Productspecification {

    /**
     * Spec pencarian nama: partial match, case-insensitive.
     * Jika keyword null/kosong, spec ini tidak ditambahkan ke query.
     *
     * SQL yang dihasilkan: LOWER(name) LIKE LOWER('%keyword%')
     */
    public static Specification<Product> nameContains(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                // Kembalikan kondisi selalu true (tidak ada filter nama)
                return criteriaBuilder.conjunction();
            }
            // LOWER() di kedua sisi memastikan case-insensitive
            // root.get("name") = kolom name bertipe String -> tidak ada masalah casting
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + keyword.trim().toLowerCase() + "%"
            );
        };
    }

    /**
     * Spec filter kategori: exact match dengan enum Category.
     * Jika category null, spec ini tidak ditambahkan ke query.
     *
     * SQL yang dihasilkan: category = 'KENDARAAN'
     */
    public static Specification<Product> categoryEquals(Category category) {
        return (root, query, criteriaBuilder) -> {
            if (category == null) {
                // Kembalikan kondisi selalu true (tidak ada filter kategori)
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get("category"), category);
        };
    }
}
