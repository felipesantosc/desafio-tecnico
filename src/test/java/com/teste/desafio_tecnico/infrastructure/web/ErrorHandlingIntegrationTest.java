package com.teste.desafio_tecnico.infrastructure.web;

import com.teste.desafio_tecnico.application.usecase.GetCouponUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ErrorHandlingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Único mock da suíte: forçar uma falha inesperada não é possível pela API real
    @MockitoBean
    private GetCouponUseCase getCouponUseCase;

    @Test
    void metodoNaoSuportadoRetorna405NoFormatoDoContrato() throws Exception {
        mockMvc.perform(put("/coupon/{id}", UUID.randomUUID()))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value(containsString("PUT")))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void contentTypeNaoSuportadoRetorna415NoFormatoDoContrato() throws Exception {
        mockMvc.perform(post("/coupon").contentType(MediaType.TEXT_PLAIN).content("oi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void erroInesperadoRetorna500SemExporDetalhesInternos() throws Exception {
        when(getCouponUseCase.execute(any())).thenThrow(new IllegalStateException("detalhe interno sensível"));

        mockMvc.perform(get("/coupon/{id}", UUID.randomUUID()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado"))
                .andExpect(jsonPath("$.message").value(not(containsString("sensível"))));
    }

    @Test
    void corpoAusenteRetorna400() throws Exception {
        mockMvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou com JSON malformado"));
    }
}
