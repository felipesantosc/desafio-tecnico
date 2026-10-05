package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountValueTest {

    @ParameterizedTest
    @ValueSource(strings = {"0.5", "0.50", "0.5001", "1", "100", "999999999.99"})
    void aceitaValorMaiorOuIgualAoMinimoSemLimiteMaximo(String value) {
        assertThat(new DiscountValue(new BigDecimal(value)).value()).isEqualByComparingTo(value);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.4999", "0.49", "0", "-1"})
    void rejeitaValorAbaixoDoMinimo(String value) {
        assertThatThrownBy(() -> new DiscountValue(new BigDecimal(value)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O valor de desconto mínimo é 0.5");
    }

    @Test
    void rejeitaValorNulo() {
        assertThatThrownBy(() -> new DiscountValue(null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("O valor de desconto é obrigatório");
    }
}
