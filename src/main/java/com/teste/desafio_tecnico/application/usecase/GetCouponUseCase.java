package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.exception.CouponNotFoundException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;

import java.util.UUID;

public class GetCouponUseCase {

    private final CouponRepository couponRepository;

    public GetCouponUseCase(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public Coupon execute(UUID id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
    }
}
