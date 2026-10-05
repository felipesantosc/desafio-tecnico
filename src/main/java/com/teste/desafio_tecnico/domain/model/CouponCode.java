package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;

public record CouponCode(String value) {

    public static final int LENGTH = 6;

    public CouponCode {
        if (value == null) {
            throw new BusinessException("O código do cupom é obrigatório");
        }
        value = value.replaceAll("[^A-Za-z0-9]", "");
        if (value.length() != LENGTH) {
            throw new BusinessException(
                    "O código do cupom deve conter exatamente %d caracteres alfanuméricos".formatted(LENGTH));
        }
    }
}
