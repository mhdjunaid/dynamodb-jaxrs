package com.jaysdk.api.repo;

import org.springframework.data.repository.CrudRepository;

import com.jaysdk.api.model.Click;


public interface ClickRepo extends CrudRepository<Click, String> {
    @Override
    void delete(Click deleted);
}