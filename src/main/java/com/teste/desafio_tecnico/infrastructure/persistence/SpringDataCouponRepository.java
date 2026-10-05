package com.teste.desafio_tecnico.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataCouponRepository extends JpaRepository<CouponEntity, UUID> {
}
