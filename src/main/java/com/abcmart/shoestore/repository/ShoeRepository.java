package com.abcmart.shoestore.repository;

import com.abcmart.shoestore.entity.ShoeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoeRepository {

    List<ShoeEntity> findAllByShoeCodes(List<Long> shoeCodes);
}
