package com.tutorial.ddd.account.infrastructure.persistence.jpa;

import com.tutorial.ddd.account.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataAccountRepository extends JpaRepository<AccountEntity, String> {
    List<AccountEntity> findByOwnerId(String ownerId);
}

