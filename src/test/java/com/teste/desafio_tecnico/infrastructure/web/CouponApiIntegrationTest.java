package com.teste.desafio_tecnico.infrastructure.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CouponApiIntegrationTest {

    private static final String FUTURE = "2099-11-04T17:14:45.180Z";
    private static final String UUID_REGEX = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$";

    @Autowired
    private MockMvc mockMvc;

    private ResultActions postCoupon(String json) throws Exception {
        return mockMvc.perform(post("/coupon").contentType(MediaType.APPLICATION_JSON).content(json));
    }

    private static String couponJson(String code, String description, String discountValue, String expirationDate) {
        return """
                {"code": %s, "description": %s, "discountValue": %s, "expirationDate": %s}
                """.formatted(code, description, discountValue, expirationDate);
    }

    private String createValidCoupon() throws Exception {
        String body = postCoupon(couponJson("\"ABC-123\"", "\"Cupom\"", "0.8", "\"" + FUTURE + "\""))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.id");
    }

    @Nested
    class Create {

        @Test
        void criaCupomComExemploDaDocumentacaoOficial() throws Exception {
            postCoupon("""
                    {
                        "code": "ABC-123",
                        "description": "Cupom de boas-vindas",
                        "discountValue": 0.8,
                        "expirationDate": "%s",
                        "published": false
                    }
                    """.formatted(FUTURE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(matchesPattern(UUID_REGEX)))
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.description").value("Cupom de boas-vindas"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.expirationDate").value("2099-11-04T17:14:45.18Z"))
                    .andExpect(jsonPath("$.status").value("ACTIVE"))
                    .andExpect(jsonPath("$.published").value(false))
                    .andExpect(jsonPath("$.redeemed").value(false));
        }

        @Test
        void criaCupomJaPublicado() throws Exception {
            postCoupon("""
                    {"code": "ABC123", "description": "x", "discountValue": 1, "expirationDate": "%s", "published": true}
                    """.formatted(FUTURE))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.published").value(true));
        }

        @Test
        void publishedAssumeFalseQuandoOmitido() throws Exception {
            postCoupon(couponJson("\"ABC123\"", "\"x\"", "1", "\"" + FUTURE + "\""))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.published").value(false));
        }

        @Test
        void aceitaDescontoNoLimiteMinimoESemLimiteMaximo() throws Exception {
            postCoupon(couponJson("\"ABC123\"", "\"x\"", "0.5", "\"" + FUTURE + "\""))
                    .andExpect(status().isCreated());
            postCoupon(couponJson("\"ABC123\"", "\"x\"", "1000000", "\"" + FUTURE + "\""))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.discountValue").value(1000000));
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @CsvSource(delimiter = '|', textBlock = """
                código curto após limpeza    | "AB-12"   | "x"   | 1    | "2099-01-01T00:00:00Z" | O código do cupom deve conter exatamente 6 caracteres alfanuméricos
                código longo                 | "ABC1234" | "x"   | 1    | "2099-01-01T00:00:00Z" | O código do cupom deve conter exatamente 6 caracteres alfanuméricos
                só caracteres especiais      | "!@#$%^"  | "x"   | 1    | "2099-01-01T00:00:00Z" | O código do cupom deve conter exatamente 6 caracteres alfanuméricos
                desconto abaixo do mínimo    | "ABC123"  | "x"   | 0.49 | "2099-01-01T00:00:00Z" | discountValue: deve ser maior que ou igual a 0.5
                data de expiração no passado | "ABC123"  | "x"   | 1    | "2020-01-01T00:00:00Z" | A data de expiração não pode estar no passado
                descrição em branco          | "ABC123"  | "   " | 1    | "2099-01-01T00:00:00Z" | A descrição do cupom é obrigatória
                data inexistente             | "ABC123"  | "x"   | 1    | "2099-06-31T00:00:00Z" | Valor inválido para o campo 'expirationDate'
                desconto não numérico        | "ABC123"  | "x"   | "abc"| "2099-01-01T00:00:00Z" | Valor inválido para o campo 'discountValue'
                """)
        void rejeitaCupomQueViolaRegra(String scenario, String code, String description, String discount,
                                       String expiration, String expectedMessage) throws Exception {
            postCoupon(couponJson(code, description, discount, expiration))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(expectedMessage))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        void rejeitaCupomSemCamposObrigatorios() throws Exception {
            postCoupon("{}")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "code: não deve ser nulo; description: não deve ser nulo; "
                                    + "discountValue: não deve ser nulo; expirationDate: não deve ser nulo"));
        }

        @Test
        void rejeitaJsonMalformado() throws Exception {
            postCoupon("{\"code\": ")
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Corpo da requisição ausente ou com JSON malformado"));
        }
    }

    @Nested
    class Get {

        @Test
        void retornaCupomExistente() throws Exception {
            String id = createValidCoupon();

            mockMvc.perform(get("/coupon/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.status").value("ACTIVE"));
        }

        @Test
        void retorna404ParaCupomInexistente() throws Exception {
            mockMvc.perform(get("/coupon/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(containsString("Cupom não encontrado")));
        }

        @Test
        void retorna400ParaIdQueNaoEUuid() throws Exception {
            mockMvc.perform(get("/coupon/123"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Valor inválido para o parâmetro 'id': '123'"));
        }
    }

    @Nested
    class Delete {

        @Test
        void deletaComSoftDeleteMantendoOsDadosDoCadastro() throws Exception {
            String id = createValidCoupon();

            mockMvc.perform(delete("/coupon/{id}", id))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            mockMvc.perform(get("/coupon/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("DELETED"))
                    .andExpect(jsonPath("$.code").value("ABC123"))
                    .andExpect(jsonPath("$.description").value("Cupom"))
                    .andExpect(jsonPath("$.discountValue").value(0.8))
                    .andExpect(jsonPath("$.expirationDate").value("2099-11-04T17:14:45.18Z"));
        }

        @Test
        void naoPermiteDeletarCupomJaDeletado() throws Exception {
            String id = createValidCoupon();
            mockMvc.perform(delete("/coupon/{id}", id)).andExpect(status().isNoContent());

            mockMvc.perform(delete("/coupon/{id}", id))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("Cupom já foi removido: " + id));
        }

        @Test
        void retorna404AoDeletarCupomInexistente() throws Exception {
            mockMvc.perform(delete("/coupon/{id}", UUID.randomUUID()))
                    .andExpect(status().isNotFound());
        }
    }
}
