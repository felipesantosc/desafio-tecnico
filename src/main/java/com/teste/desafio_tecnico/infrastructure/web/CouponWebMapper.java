package com.teste.desafio_tecnico.infrastructure.web;

import com.teste.desafio_tecnico.application.usecase.CreateCouponCommand;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.infrastructure.web.api.model.CouponRequest;
import com.teste.desafio_tecnico.infrastructure.web.api.model.CouponResponse;
import com.teste.desafio_tecnico.infrastructure.web.api.model.CouponStatus;

final class CouponWebMapper {

    private CouponWebMapper() {
    }

    static CreateCouponCommand toCommand(CouponRequest request) {
        return new CreateCouponCommand(
                request.getCode(),
                request.getDescription(),
                request.getDiscountValue(),
                request.getExpirationDate(),
                Boolean.TRUE.equals(request.getPublished()));
    }

    static CouponResponse toResponse(Coupon coupon) {
        return new CouponResponse(
                coupon.getId(),
                coupon.getCode().value(),
                coupon.getDescription(),
                coupon.getDiscountValue().value(),
                coupon.getExpirationDate(),
                CouponStatus.valueOf(coupon.getStatus().name()),
                coupon.isPublished(),
                coupon.isRedeemed());
    }
}
