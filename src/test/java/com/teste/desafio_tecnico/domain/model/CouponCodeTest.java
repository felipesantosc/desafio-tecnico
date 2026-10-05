package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponCodeTest {

    @ParameterizedTest
    @CsvSource({
            "ABC123,      ABC123",
            "ABC-123,     ABC123",
            "A@B#C-1!2.3, ABC123",
            "' AB C1 23 ', ABC123",
            "abc123,      abc123"
    })
    void removeCaracteresEspeciaisMantendoSeisAlfanumericos(String raw, String expected) {
        assertThat(new CouponCode(raw).value()).isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ABC12", "ABC-12", "ABC1234", "ABC-1234", "", "!@#$%^&*", "ÁBC123"})
    void rejeitaCodigoQueNaoTemSeisAlfanumericosAposLimpeza(String raw) {
        assertThatThrownBy(() -> new CouponCode(raw))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exatamente 6 caracteres");
    }

    @Test
    void rejeitaCodigoNulo() {
        assertThatThrownBy(() -> new CouponCode(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O código do cupom é obrigatório");
    }

    @Test
    void comparaPeloValorLimpo() {
        assertThat(new CouponCode("ABC-123")).isEqualTo(new CouponCode("ABC123"));
    }
}
