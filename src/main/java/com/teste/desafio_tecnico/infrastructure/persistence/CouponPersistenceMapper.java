package com.teste.desafio_tecnico.infrastructure.persistence;

import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.model.CouponCode;
import com.teste.desafio_tecnico.domain.model.DiscountValue;

import java.math.BigDecimal;

final class CouponPersistenceMapper {

    private CouponPersistenceMapper() {
    }

    static CouponEntity toEntity(Coupon coupon) {
        CouponEntity entity = new CouponEntity();
        entity.setId(coupon.getId());
        entity.setCode(coupon.getCode().value());
        entity.setDescription(coupon.getDescription());
        entity.setDiscountValue(coupon.getDiscountValue().value());
        entity.setExpirationDate(coupon.getExpirationDate());
        entity.setStatus(coupon.getStatus());
        entity.setPublished(coupon.isPublished());
        entity.setRedeemed(coupon.isRedeemed());
        entity.setVersion(coupon.getVersion());
        return entity;
    }

    static Coupon toDomain(CouponEntity entity) {
        return Coupon.restore(
                entity.getId(),
                new CouponCode(entity.getCode()),
                entity.getDescription(),
                new DiscountValue(normalize(entity.getDiscountValue())),
                entity.getExpirationDate(),
                entity.getStatus(),
                entity.isPublished(),
                entity.isRedeemed(),
                entity.getVersion());
    }

    // A coluna tem escala fixa (0.8 volta do banco como 0.8000); remove os zeros à direita sem usar notação científica
    private static BigDecimal normalize(BigDecimal value) {
        BigDecimal stripped = value.stripTrailingZeros();
        return stripped.scale() < 0 ? stripped.setScale(0) : stripped;
    }
}
