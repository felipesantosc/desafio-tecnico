package com.teste.desafio_tecnico.domain.repository;

import com.teste.desafio_tecnico.domain.model.Coupon;

import java.util.Optional;
import java.util.UUID;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findById(UUID id);
}
