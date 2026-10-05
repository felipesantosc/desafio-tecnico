package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.exception.BusinessException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CreateCouponUseCaseTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC);
    private static final OffsetDateTime FUTURE = OffsetDateTime.parse("2026-12-31T23:59:59Z");

    private final InMemoryCouponRepository repository = new InMemoryCouponRepository();
    private final CreateCouponUseCase useCase = new CreateCouponUseCase(repository, CLOCK);

    @Test
    void criaEPersisteCupomComCodigoLimpo() {
        Coupon created = useCase.execute(new CreateCouponCommand("ABC-123", "Boas-vindas",
                new BigDecimal("0.8"), FUTURE, true));

        Coupon stored = repository.findById(created.getId()).orElseThrow();
        assertThat(stored.getCode().value()).isEqualTo("ABC123");
        assertThat(stored.getDescription()).isEqualTo("Boas-vindas");
        assertThat(stored.getDiscountValue().value()).isEqualByComparingTo("0.8");
        assertThat(stored.getExpirationDate()).isEqualTo(FUTURE);
        assertThat(stored.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(stored.isPublished()).isTrue();
    }

    @Test
    void naoPersisteCupomComDataNoPassado() {
        CreateCouponCommand command = new CreateCouponCommand("ABC123", "Expirado",
                BigDecimal.ONE, OffsetDateTime.parse("2025-12-31T23:59:59Z"), false);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(BusinessException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void naoPersisteCupomComDescontoAbaixoDoMinimo() {
        CreateCouponCommand command = new CreateCouponCommand("ABC123", "Desconto baixo",
                new BigDecimal("0.49"), FUTURE, false);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(BusinessException.class);
        assertThat(repository.count()).isZero();
    }

    @Test
    void naoPersisteCupomComCodigoInvalido() {
        CreateCouponCommand command = new CreateCouponCommand("AB-12", "Código curto",
                BigDecimal.ONE, FUTURE, false);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(BusinessException.class);
        assertThat(repository.count()).isZero();
    }
}
