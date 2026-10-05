package com.teste.desafio_tecnico.application.usecase;

import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Fake da porta de repositório. Guarda cópias (snapshots), como um banco faria:
 * alterar um cupom em memória sem chamar save() não altera o que está armazenado.
 */
class InMemoryCouponRepository implements CouponRepository {

    private final Map<UUID, Coupon> storage = new HashMap<>();

    @Override
    public Coupon save(Coupon coupon) {
        storage.put(coupon.getId(), copyOf(coupon));
        return copyOf(coupon);
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return Optional.ofNullable(storage.get(id)).map(InMemoryCouponRepository::copyOf);
    }

    int count() {
        return storage.size();
    }

    private static Coupon copyOf(Coupon coupon) {
        return Coupon.restore(coupon.getId(), coupon.getCode(), coupon.getDescription(), coupon.getDiscountValue(),
                coupon.getExpirationDate(), coupon.getStatus(), coupon.isPublished(), coupon.isRedeemed(),
                coupon.getVersion());
    }
}
