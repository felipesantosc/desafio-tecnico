package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponCode;
import com.teste.desafio_tecnico.domain.model.DiscountValue;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;

import java.time.Clock;

public class CreateCouponUseCase {

    private final CouponRepository couponRepository;
    private final Clock clock;

    public CreateCouponUseCase(CouponRepository couponRepository, Clock clock) {
        this.couponRepository = couponRepository;
        this.clock = clock;
    }

    public Coupon execute(CreateCouponCommand command) {
        Coupon coupon = Coupon.create(
                new CouponCode(command.code()),
                command.description(),
                new DiscountValue(command.discountValue()),
                command.expirationDate(),
                command.published(),
                clock);
        return couponRepository.save(coupon);
    }
}
