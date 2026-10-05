package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.exception.CouponNotFoundException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponCode;
import com.teste.desafio_tecnico.domain.model.DiscountValue;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GetCouponUseCaseTest {

    private final InMemoryCouponRepository repository = new InMemoryCouponRepository();
    private final GetCouponUseCase useCase = new GetCouponUseCase(repository);

    @Test
    void retornaCupomExistente() {
        Coupon saved = repository.save(Coupon.create(new CouponCode("ABC123"), "Cupom",
                new DiscountValue(BigDecimal.ONE), OffsetDateTime.parse("2099-01-01T00:00:00Z"), false,
                Clock.systemUTC()));

        Coupon found = useCase.execute(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCode()).isEqualTo(saved.getCode());
    }

    @Test
    void falhaQuandoCupomNaoExiste() {
        assertThatThrownBy(() -> useCase.execute(UUID.randomUUID()))
                .isInstanceOf(CouponNotFoundException.class);
    }
}
