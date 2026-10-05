package com.teste.desafio_tecnico.domain.exception;

import java.util.UUID;

public class CouponConcurrentModificationException extends RuntimeException {

    public CouponConcurrentModificationException(UUID id) {
        super("O cupom %s foi modificado por outra operação ao mesmo tempo. Consulte-o novamente.".formatted(id));
    }
}
