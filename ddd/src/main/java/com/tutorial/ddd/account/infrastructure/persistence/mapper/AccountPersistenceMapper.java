package com.tutorial.ddd.account.infrastructure.persistence.mapper;

import com.tutorial.ddd.account.domain.Account;
import com.tutorial.ddd.account.domain.AccountId;
import com.tutorial.ddd.account.infrastructure.persistence.entity.AccountEntity;
import com.tutorial.ddd.shared.config.DddMapperConfig;
import com.tutorial.ddd.shared.domain.Money;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper de persistencia para el bounded context account.
 *
 * Se mantiene simple con métodos default para evitar expression:
 * - Account usa API de dominio (id(), balance(), ...), no getters JavaBean.
 * - AccountEntity sí usa getters/setters JavaBean para JPA.
 */
@Mapper(config = DddMapperConfig.class)
public interface AccountPersistenceMapper {

    default AccountEntity toEntity(Account account) {
        return new AccountEntity(
                account.id().value(),
                account.ownerId(),
                account.ownerName(),
                account.balance().amount(),
                account.balance().currency(),
                account.isActive(),
                account.createdAt()
        );
    }

    default Account toDomain(AccountEntity entity) {
        return Account.reconstitute(
                AccountId.of(entity.getId()),
                entity.getOwnerId(),
                entity.getOwnerName(),
                new Money(entity.getBalance(), entity.getCurrency()),
                entity.isActive(),
                entity.getCreatedAt()
        );
    }

    List<Account> toDomainList(List<AccountEntity> entities);
}
