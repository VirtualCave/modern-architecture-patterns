package com.tutorial.cqrs;

import com.tutorial.cqrs.product.domain.Product;
import com.tutorial.cqrs.product.domain.ProductId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductDomainTest {

    @Test
    void createProductAndUpdatePrice() {
        var p = Product.create(ProductId.generate(), "Laptop", "Pro", BigDecimal.valueOf(2000), 3);

        p.updatePrice(BigDecimal.valueOf(1800));
        p.addStock(2);

        assertThat(p.price()).isEqualByComparingTo("1800");
        assertThat(p.stock()).isEqualTo(5);
    }

    @Test
    void invalidPriceShouldFail() {
        assertThatThrownBy(() -> Product.create(ProductId.generate(), "A", "B", BigDecimal.ZERO, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

