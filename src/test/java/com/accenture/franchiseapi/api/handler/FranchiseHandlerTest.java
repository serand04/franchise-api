package com.accenture.franchiseapi.api.handler;

import com.accenture.franchiseapi.api.router.FranchiseRouter;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.TopStockProduct;
import com.accenture.franchiseapi.domain.port.in.FranchiseUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({FranchiseRouter.class, FranchiseHandler.class})
@DisplayName("FranchiseHandler - WebFlux Tests")
class FranchiseHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private FranchiseUseCase franchiseUseCase;

    private static final String BASE = "/api/v1/franchises";

    // ── POST /franchises ──────────────────────────────────────────────────────

    @Test
    @DisplayName("POST /franchises: should return 201 with created franchise")
    void addFranchise_returns201() {
        Franchise created = Franchise.builder()
                .id("f-1").name("McTest").branches(new ArrayList<>()).build();

        when(franchiseUseCase.addFranchise("McTest")).thenReturn(Mono.just(created));

        webTestClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"McTest\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("f-1")
                .jsonPath("$.name").isEqualTo("McTest");
    }

    @Test
    @DisplayName("POST /franchises: should return 400 when name is blank")
    void addFranchise_returns400WhenNameBlank() {
        webTestClient.post().uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ── PATCH /franchises/{franchiseId} ───────────────────────────────────────

    @Test
    @DisplayName("PATCH /franchises/{id}: should return 200 with updated franchise")
    void updateFranchiseName_returns200() {
        Franchise updated = Franchise.builder()
                .id("f-1").name("NewName").branches(new ArrayList<>()).build();

        when(franchiseUseCase.updateFranchiseName("f-1", "NewName")).thenReturn(Mono.just(updated));

        webTestClient.patch().uri(BASE + "/f-1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"NewName\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("NewName");
    }

    @Test
    @DisplayName("PATCH /franchises/{id}: should return 404 when franchise not found")
    void updateFranchiseName_returns404() {
        when(franchiseUseCase.updateFranchiseName(eq("missing"), any()))
                .thenReturn(Mono.error(new RuntimeException("Franchise not found: missing")));

        webTestClient.patch().uri(BASE + "/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\":\"X\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    // ── GET /franchises/{id}/top-stock-products ────────────────────────────────

    @Test
    @DisplayName("GET /franchises/{id}/top-stock-products: should return product list")
    void getTopStockProducts_returns200() {
        TopStockProduct top = TopStockProduct.builder()
                .branchId("b-1").branchName("North").productId("p-1")
                .productName("Burger").stock(100).build();

        when(franchiseUseCase.getTopStockProductsPerBranch("f-1"))
                .thenReturn(Flux.just(top));

        webTestClient.get().uri(BASE + "/f-1/top-stock-products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].branchName").isEqualTo("North")
                .jsonPath("$[0].stock").isEqualTo(100);
    }
}
