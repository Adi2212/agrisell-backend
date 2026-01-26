package com.agridev.repository;

import com.agridev.model.Category;
import com.agridev.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByUserId(Long id);

    List<Product> findByCategory(Category category);

    int countByUserId(Long id);

    long countBy();

}
