package com.teste.desafio_tecnico.domain.exception;

import java.util.UUID;

public class CouponNotFoundException extends RuntimeException {

    public CouponNotFoundException(UUID id) {
        super("Cupom não encontrado: " + id);
    }
}
