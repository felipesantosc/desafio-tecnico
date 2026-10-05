package com.teste.desafio_tecnico.infrastructure.persistence;

import com.teste.desafio_tecnico.domain.exception.CouponConcurrentModificationException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponCode;
import com.teste.desafio_tecnico.domain.model.CouponStatus;
import com.teste.desafio_tecnico.domain.model.DiscountValue;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CouponRepositoryAdapterIntegrationTest {

    @Autowired
    private CouponRepository repository;

    private Coupon newCoupon(String discountValue) {
        return Coupon.create(new CouponCode("ABC123"), "Cupom", new DiscountValue(new BigDecimal(discountValue)),
                OffsetDateTime.parse("2099-01-01T00:00:00Z"), false, Clock.systemUTC());
    }

    @Test
    void salvaERecuperaTodosOsCampos() {
        Coupon saved = repository.save(newCoupon("0.8"));

        Coupon found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getCode()).isEqualTo(new CouponCode("ABC123"));
        assertThat(found.getDescription()).isEqualTo("Cupom");
        assertThat(found.getExpirationDate()).isEqualTo(OffsetDateTime.parse("2099-01-01T00:00:00Z"));
        assertThat(found.getStatus()).isEqualTo(CouponStatus.ACTIVE);
        assertThat(found.getVersion()).isNotNull();
    }

    @ParameterizedTest
    @CsvSource({"0.8, 0.8", "1.50, 1.5", "10, 10", "0.5555, 0.5555"})
    void preservaValorDeDescontoSemArredondarNemZerosExtras(String sent, String expected) {
        Coupon saved = repository.save(newCoupon(sent));

        BigDecimal found = repository.findById(saved.getId()).orElseThrow().getDiscountValue().value();

        assertThat(found.toPlainString()).isEqualTo(expected);
    }

    @Test
    void recuperaCupomQueJaExpirouDepoisDeCriado() {
        Coupon expired = Coupon.restore(UUID.randomUUID(), new CouponCode("OLD123"), "Antigo",
                new DiscountValue(BigDecimal.ONE), OffsetDateTime.parse("2020-01-01T00:00:00Z"),
                CouponStatus.ACTIVE, false, false, null);
        repository.save(expired);

        assertThat(repository.findById(expired.getId())).isPresent();
    }

    @Test
    void retornaVazioParaIdInexistente() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    void lockOtimistaImpedeDoisDeletesConcorrentesDoMesmoCupom() {
        Coupon saved = repository.save(newCoupon("1"));

        Coupon readByRequestA = repository.findById(saved.getId()).orElseThrow();
        Coupon readByRequestB = repository.findById(saved.getId()).orElseThrow();
        readByRequestA.delete();
        readByRequestB.delete();

        repository.save(readByRequestA);

        assertThatThrownBy(() -> repository.save(readByRequestB))
                .isInstanceOf(CouponConcurrentModificationException.class);
        assertThat(repository.findById(saved.getId()).orElseThrow().getStatus()).isEqualTo(CouponStatus.DELETED);
    }
}
