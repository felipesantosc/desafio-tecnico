package com.teste.desafio_tecnico.infrastructure.persistence;

import com.teste.desafio_tecnico.domain.exception.CouponConcurrentModificationException;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {

    private final SpringDataCouponRepository springDataRepository;

    @Override
    public Coupon save(Coupon coupon) {
        try {
            CouponEntity saved = springDataRepository.saveAndFlush(CouponPersistenceMapper.toEntity(coupon));
            return CouponPersistenceMapper.toDomain(saved);
        } catch (OptimisticLockingFailureException ex) {
            throw new CouponConcurrentModificationException(coupon.getId());
        }
    }

    @Override
    public Optional<Coupon> findById(UUID id) {
        return springDataRepository.findById(id).map(CouponPersistenceMapper::toDomain);
    }
}
