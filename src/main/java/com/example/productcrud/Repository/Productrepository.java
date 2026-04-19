package com.example.productcrud.Repository;

import com.example.productcrud.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Productrepository extend dua interface:
 * - JpaRepository           : operasi CRUD standar (findAll, findById, save, deleteById)
 * - JpaSpecificationExecutor: menambah kemampuan query dinamis via Specification
 *                             method utama: findAll(Specification, Pageable)
 *
 * Tidak perlu @Query manual — Specification yang mengurus logika filter-nya,
 * sehingga tidak ada masalah type casting dengan PostgreSQL.
 */
@Repository
public interface Productrepository extends JpaRepository<Product, Long>,
        JpaSpecificationExecutor<Product> {
}