package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.exception.CouponAlreadyDeletedException;
import com.teste.desafio_tecnico.domain.exception.CouponNotFoundException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponCode;
import com.teste.desafio_tecnico.domain.model.CouponStatus;
import com.teste.desafio_tecnico.domain.model.DiscountValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeleteCouponUseCaseTest {

    private final InMemoryCouponRepository repository = new InMemoryCouponRepository();
    private final DeleteCouponUseCase useCase = new DeleteCouponUseCase(repository);

    private Coupon existing;

    @BeforeEach
    void setUp() {
        existing = repository.save(Coupon.create(new CouponCode("ABC123"), "Cupom",
                new DiscountValue(BigDecimal.ONE), OffsetDateTime.parse("2099-01-01T00:00:00Z"), false,
                Clock.systemUTC()));
    }

    @Test
    void persisteSoftDeleteMantendoOCupomArmazenado() {
        useCase.execute(existing.getId());

        Coupon stored = repository.findById(existing.getId()).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(CouponStatus.DELETED);
        assertThat(stored.getCode().value()).isEqualTo("ABC123");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void naoPermiteDeletarCupomJaDeletado() {
        useCase.execute(existing.getId());

        assertThatThrownBy(() -> useCase.execute(existing.getId()))
                .isInstanceOf(CouponAlreadyDeletedException.class);
    }

    @Test
    void falhaQuandoCupomNaoExiste() {
        UUID unknownId = UUID.randomUUID();

        assertThatThrownBy(() -> useCase.execute(unknownId))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining(unknownId.toString());
    }
}
