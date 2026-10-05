package com.teste.desafio_tecnico.infrastructure.config;

import com.teste.desafio_tecnico.application.usecase.CreateCouponUseCase;
import com.teste.desafio_tecnico.application.usecase.DeleteCouponUseCase;
import com.teste.desafio_tecnico.application.usecase.GetCouponUseCase;
import com.teste.desafio_tecnico.domain.repository.CouponRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreateCouponUseCase createCouponUseCase(CouponRepository couponRepository, Clock clock) {
        return new CreateCouponUseCase(couponRepository, clock);
    }

    @Bean
    public GetCouponUseCase getCouponUseCase(CouponRepository couponRepository) {
        return new GetCouponUseCase(couponRepository);
    }

    @Bean
    public DeleteCouponUseCase deleteCouponUseCase(CouponRepository couponRepository) {
        return new DeleteCouponUseCase(couponRepository);
    }
}
