package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;

import java.math.BigDecimal;

public record DiscountValue(BigDecimal value) {

    public static final BigDecimal MINIMUM = new BigDecimal("0.5");

    public DiscountValue {
        if (value == null) {
            throw new BusinessException("O valor de desconto é obrigatório");
        }
        if (value.compareTo(MINIMUM) < 0) {
            throw new BusinessException("O valor de desconto mínimo é " + MINIMUM);
        }
    }
}
