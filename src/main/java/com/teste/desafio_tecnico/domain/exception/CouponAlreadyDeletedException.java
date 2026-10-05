package com.teste.desafio_tecnico.domain.exception;

import java.util.UUID;

public class CouponAlreadyDeletedException extends BusinessException {

    public CouponAlreadyDeletedException(UUID id) {
        super("Cupom já foi removido: " + id);
    }
}
