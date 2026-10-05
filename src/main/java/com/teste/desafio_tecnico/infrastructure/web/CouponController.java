package com.teste.desafio_tecnico.infrastructure.web;

import com.teste.desafio_tecnico.application.usecase.CreateCouponUseCase;
import com.teste.desafio_tecnico.application.usecase.DeleteCouponUseCase;
import com.teste.desafio_tecnico.application.usecase.GetCouponUseCase;
import com.teste.desafio_tecnico.domain.model.Coupon;
import com.teste.desafio_tecnico.infrastructure.web.api.CouponApi;
import com.teste.desafio_tecnico.infrastructure.web.api.model.CouponRequest;
import com.teste.desafio_tecnico.infrastructure.web.api.model.CouponResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

    private final CreateCouponUseCase createCouponUseCase;
    private final GetCouponUseCase getCouponUseCase;
    private final DeleteCouponUseCase deleteCouponUseCase;

    @Override
    public ResponseEntity<CouponResponse> createCoupon(CouponRequest couponRequest) {
        Coupon coupon = createCouponUseCase.execute(CouponWebMapper.toCommand(couponRequest));
        return ResponseEntity.status(HttpStatus.CREATED).body(CouponWebMapper.toResponse(coupon));
    }

    @Override
    public ResponseEntity<CouponResponse> getCouponById(UUID id) {
        return ResponseEntity.ok(CouponWebMapper.toResponse(getCouponUseCase.execute(id)));
    }

    @Override
    public ResponseEntity<Void> deleteCoupon(UUID id) {
        deleteCouponUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
