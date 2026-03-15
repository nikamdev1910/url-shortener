package com.devn.urlshortener.repository;

import com.devn.urlshortener.entity.Click;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClickRepository extends JpaRepository<Click, Long> {

    List<Click> findByUrlId(Long urlId);

    long countByUrlId(Long urlId);

    @Query("SELECT c.country, COUNT(c) FROM Click c WHERE c.url.id = :urlId GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> countClicksByCountry(@Param("urlId") Long urlId);

    @Query("SELECT c.referrer, COUNT(c) FROM Click c WHERE c.url.id = :urlId GROUP BY c.referrer ORDER BY COUNT(c) DESC")
    List<Object[]> countClicksByReferrer(@Param("urlId") Long urlId);

    @Query("SELECT DATE(c.clickedAt), COUNT(c) FROM Click c WHERE c.url.id = :urlId GROUP BY DATE(c.clickedAt) ORDER BY DATE(c.clickedAt)")
    List<Object[]> countClicksByDate(@Param("urlId") Long urlId);
}