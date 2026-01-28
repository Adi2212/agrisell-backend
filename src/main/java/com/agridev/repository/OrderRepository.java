package com.agridev.repository;

import com.agridev.model.Order;
import com.agridev.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    int countByUserId(Long userId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.userId = :userId")
    double sumTotalByUserId(Long userId);

    // returns: [date (String/Date), status (String), cnt (BigInteger/Number)]
    @Query(value = """
        SELECT DATE(o.created_at) AS dt,
               o.status AS status,
               COUNT(*) AS cnt
        FROM orders o
        WHERE o.created_at BETWEEN :start AND :end
        GROUP BY DATE(o.created_at), o.status
        ORDER BY DATE(o.created_at)
        """, nativeQuery = true)
    List<Object[]> countByDateAndStatus(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Farmer Orders: Orders containing farmer’s products
    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN o.items i
    WHERE i.product.user.id = :farmerId
""")
    List<Order> findOrdersByFarmerId(@Param("farmerId") Long farmerId);

    long countByStatus(Status status);

    @Query("SELECT COALESCE(SUM(o.totalAmount),0) FROM Order o WHERE o.paymentStatus='PAID'")
    double totalRevenue();

    @Query("""
    SELECT COUNT(o)
    FROM Order o
    JOIN o.items i
    WHERE i.product.user.id = :farmerId
    AND o.status = :status
""")
    long countOrdersByFarmerAndStatus(Long farmerId, Status status);


    @Query("""
SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END
FROM Order o
JOIN o.items i
WHERE o.userId = :buyerId
AND i.product.id = :productId
AND o.status = 'DELIVERED'
""")
    boolean hasDeliveredOrder(Long buyerId, Long productId);



}
