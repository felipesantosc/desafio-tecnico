package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;
import com.teste.desafio_tecnico.domain.exception.CouponAlreadyDeletedException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);
    private static final OffsetDateTime FUTURE = OffsetDateTime.parse("2026-12-31T23:59:59Z");

    private static Coupon createWithExpiration(OffsetDateTime expirationDate) {
        return Coupon.create(new CouponCode("ABC-123"), "Cupom de teste",
                new DiscountValue(new BigDecimal("0.8")), expirationDate, false, CLOCK);
    }

    @Nested
    class Create {

        @Test
        void criaCupomAtivoNaoResgatadoComIdentidadePropria() {
            Coupon coupon = createWithExpiration(FUTURE);

            assertThat(coupon.getId()).isNotNull();
            assertThat(coupon.getCode().value()).isEqualTo("ABC123");
            assertThat(coupon.getDescription()).isEqualTo("Cupom de teste");
            assertThat(coupon.getDiscountValue().value()).isEqualByComparingTo("0.8");
            assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE);
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.ACTIVE);
            assertThat(coupon.isRedeemed()).isFalse();
            assertThat(coupon.isDeleted()).isFalse();
        }

        @Test
        void cadaCupomCriadoTemIdDiferente() {
            assertThat(createWithExpiration(FUTURE).getId()).isNotEqualTo(createWithExpiration(FUTURE).getId());
        }

        @Test
        void podeSerCriadoJaPublicado() {
            Coupon coupon = Coupon.create(new CouponCode("ABC123"), "Publicado",
                    new DiscountValue(BigDecimal.ONE), FUTURE, true, CLOCK);

            assertThat(coupon.isPublished()).isTrue();
        }

        @Test
        void aceitaExpiracaoExatamenteNoMomentoAtual() {
            OffsetDateTime now = OffsetDateTime.ofInstant(NOW, ZoneOffset.UTC);

            assertThatCode(() -> createWithExpiration(now)).doesNotThrowAnyException();
        }

        @Test
        void rejeitaExpiracaoUmSegundoNoPassado() {
            OffsetDateTime oneSecondAgo = OffsetDateTime.ofInstant(NOW.minusSeconds(1), ZoneOffset.UTC);

            assertThatThrownBy(() -> createWithExpiration(oneSecondAgo))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("A data de expiração não pode estar no passado");
        }

        @Test
        void comparaExpiracaoPeloInstanteIndependenteDoFuso() {
            // 08:59 em São Paulo (-03:00) = 11:59Z, que é antes de NOW (12:00Z)
            OffsetDateTime pastInSaoPaulo = OffsetDateTime.parse("2026-01-01T08:59:00-03:00");

            assertThatThrownBy(() -> createWithExpiration(pastInSaoPaulo))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        void rejeitaExpiracaoNula() {
            assertThatThrownBy(() -> createWithExpiration(null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("A data de expiração é obrigatória");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", "   ", "\t\n"})
        void rejeitaDescricaoAusenteOuEmBranco(String description) {
            assertThatThrownBy(() -> Coupon.create(new CouponCode("ABC123"), description,
                    new DiscountValue(BigDecimal.ONE), FUTURE, false, CLOCK))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("A descrição do cupom é obrigatória");
        }

        @Test
        void rejeitaCodigoNulo() {
            assertThatThrownBy(() -> Coupon.create(null, "x", new DiscountValue(BigDecimal.ONE), FUTURE, false, CLOCK))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("O código do cupom é obrigatório");
        }

        @Test
        void rejeitaDescontoNulo() {
            assertThatThrownBy(() -> Coupon.create(new CouponCode("ABC123"), "x", null, FUTURE, false, CLOCK))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("O valor de desconto é obrigatório");
        }
    }

    @Nested
    class Delete {

        @Test
        void softDeleteMudaStatusMantendoTodosOsDados() {
            Coupon coupon = createWithExpiration(FUTURE);

            coupon.delete();

            assertThat(coupon.isDeleted()).isTrue();
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
            assertThat(coupon.getCode().value()).isEqualTo("ABC123");
            assertThat(coupon.getDescription()).isEqualTo("Cupom de teste");
            assertThat(coupon.getDiscountValue().value()).isEqualByComparingTo("0.8");
            assertThat(coupon.getExpirationDate()).isEqualTo(FUTURE);
        }

        @Test
        void naoPermiteDeletarDuasVezes() {
            Coupon coupon = createWithExpiration(FUTURE);
            coupon.delete();

            assertThatThrownBy(coupon::delete)
                    .isInstanceOf(CouponAlreadyDeletedException.class)
                    .hasMessageContaining(coupon.getId().toString());
            assertThat(coupon.getStatus()).isEqualTo(CouponStatus.DELETED);
        }
    }

    @Nested
    class Restore {

        @Test
        void reconstituiCupomJaExpiradoSemAplicarRegrasDeCriacao() {
            OffsetDateTime expired = OffsetDateTime.parse("2020-01-01T00:00:00Z");

            Coupon coupon = Coupon.restore(UUID.randomUUID(), new CouponCode("ABC123"), "Antigo",
                    new DiscountValue(BigDecimal.ONE), expired, CouponStatus.ACTIVE, false, false, 3L);

            assertThat(coupon.getExpirationDate()).isEqualTo(expired);
            assertThat(coupon.getVersion()).isEqualTo(3L);
        }

        @Test
        void cupomReconstituidoComoDeletadoNaoPodeSerDeletadoNovamente() {
            Coupon coupon = Coupon.restore(UUID.randomUUID(), new CouponCode("ABC123"), "Removido",
                    new DiscountValue(BigDecimal.ONE), FUTURE, CouponStatus.DELETED, false, false, 1L);

            assertThatThrownBy(coupon::delete).isInstanceOf(CouponAlreadyDeletedException.class);
        }
    }
}
