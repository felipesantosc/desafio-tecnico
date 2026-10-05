package com.teste.desafio_tecnico.domain.model;

import com.teste.desafio_tecnico.domain.exception.BusinessException;
import com.teste.desafio_tecnico.domain.exception.CouponAlreadyDeletedException;
import lombok.Getter;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
public class Coupon {

    private final UUID id;
    private final CouponCode code;
    private final String description;
    private final DiscountValue discountValue;
    private final OffsetDateTime expirationDate;
    private final boolean published;
    private final boolean redeemed;
    private final Long version;
    private CouponStatus status;

    private Coupon(UUID id, CouponCode code, String description, DiscountValue discountValue,
                   OffsetDateTime expirationDate, CouponStatus status, boolean published,
                   boolean redeemed, Long version) {
        this.id = id;
        this.code = code;
        this.description = description;
        this.discountValue = discountValue;
        this.expirationDate = expirationDate;
        this.status = status;
        this.published = published;
        this.redeemed = redeemed;
        this.version = version;
    }

    public static Coupon create(CouponCode code, String description, DiscountValue discountValue,
                                OffsetDateTime expirationDate, boolean published, Clock clock) {
        return new Coupon(
                UUID.randomUUID(),
                requireNonNull(code, "O código do cupom é obrigatório"),
                validateDescription(description),
                requireNonNull(discountValue, "O valor de desconto é obrigatório"),
                validateExpirationDate(expirationDate, clock),
                CouponStatus.ACTIVE,
                published,
                false,
                null);
    }

    public static Coupon restore(UUID id, CouponCode code, String description, DiscountValue discountValue,
                                 OffsetDateTime expirationDate, CouponStatus status, boolean published,
                                 boolean redeemed, Long version) {
        return new Coupon(id, code, description, discountValue, expirationDate, status,
                published, redeemed, version);
    }

    public void delete() {
        if (isDeleted()) {
            throw new CouponAlreadyDeletedException(id);
        }
        status = CouponStatus.DELETED;
    }

    public boolean isDeleted() {
        return status == CouponStatus.DELETED;
    }

    private static <T> T requireNonNull(T value, String message) {
        if (value == null) {
            throw new BusinessException(message);
        }
        return value;
    }

    private static String validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessException("A descrição do cupom é obrigatória");
        }
        return description;
    }

    private static OffsetDateTime validateExpirationDate(OffsetDateTime expirationDate, Clock clock) {
        requireNonNull(expirationDate, "A data de expiração é obrigatória");
        if (expirationDate.isBefore(OffsetDateTime.now(clock))) {
            throw new BusinessException("A data de expiração não pode estar no passado");
        }
        return expirationDate;
    }
}
