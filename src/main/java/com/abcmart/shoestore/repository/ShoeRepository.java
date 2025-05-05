package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.domain.Shoe;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoeRepository {

    Shoe save(Shoe shoe);

    Shoe findByShoeCode(Long shoeCode);

    List<Shoe> findAllByShoeCodes(List<Long> shoeCodes);
}
