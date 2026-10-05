package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.exception.CouponNotFoundException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;

import java.util.UUID;

public class DeleteCouponUseCase {

    private final CouponRepository couponRepository;

    public DeleteCouponUseCase(CouponRepository couponRepository) {
        this.couponRepository = couponRepository;
    }

    public void execute(UUID id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponNotFoundException(id));
        coupon.delete();
        couponRepository.save(coupon);
    }
}
