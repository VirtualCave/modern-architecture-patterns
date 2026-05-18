package com.tutorial.ddd.account.infrastructure.persistence;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.domain.AccountId;
import com.tutorial.ddd.account.domain.repository.AccountRepository;
import com.tutorial.ddd.account.infrastructure.persistence.entity.AccountEntity;
import com.tutorial.ddd.account.infrastructure.persistence.jpa.SpringDataAccountRepository;
import com.tutorial.ddd.account.infrastructure.persistence.mapper.AccountPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepository {

    private final SpringDataAccountRepository repository;
    private final AccountPersistenceMapper mapper;

    public AccountRepositoryAdapter(SpringDataAccountRepository repository,
                                    AccountPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Account save(Account account) {
        AccountEntity entity = mapper.toEntity(account);
        var saved = repository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<Account> findByOwnerId(String ownerId) {
        return mapper.toDomainList(repository.findByOwnerId(ownerId));
    }
}

