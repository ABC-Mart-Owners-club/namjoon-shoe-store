package com.abcmart.shoestore.shoe.repository;

import com.abcmart.shoestore.shoe.domain.Shoe;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoeRepository {

    Shoe save(Shoe shoe);

    Optional<Shoe> findByShoeCode(Long shoeCode);

    List<Shoe> findAllByShoeCodes(List<Long> shoeCodes);
}
