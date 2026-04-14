package com.accenture.franchiseapi.application.usecase;

import com.accenture.franchiseapi.domain.model.Branch;
import com.accenture.franchiseapi.domain.model.Franchise;
import com.accenture.franchiseapi.domain.model.Product;
import com.accenture.franchiseapi.domain.port.out.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("FranchiseUseCaseImpl - Unit Tests")
class FranchiseUseCaseImplTest {

    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private FranchiseUseCaseImpl franchiseUseCase;

    private Franchise sampleFranchise;
    private Branch sampleBranch;

    @BeforeEach
    void setUp() {
        Product sampleProduct = Product.builder()
                .id("prod-1")
                .name("Burger")
                .stock(100)
                .build();

        sampleBranch = Branch.builder()
                .id("branch-1")
                .name("Branch North")
                .products(new ArrayList<>(List.of(sampleProduct)))
                .build();

        sampleFranchise = Franchise.builder()
                .id("franchise-1")
                .name("McTest")
                .branches(new ArrayList<>(List.of(sampleBranch)))
                .build();
    }

    // ── addFranchise ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("addFranchise: should create and save a new franchise")
    void addFranchise_shouldCreateAndSave() {
        when(franchiseRepository.save(any(Franchise.class)))
                .thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.addFranchise("MyFranchise"))
                .assertNext(franchise -> {
                    assertThat(franchise.getName()).isEqualTo("MyFranchise");
                    assertThat(franchise.getId()).isNotBlank();
                })
                .verifyComplete();
    }

    // ── updateFranchiseName ───────────────────────────────────────────────────

    @Test
    @DisplayName("updateFranchiseName: should update the name when franchise exists")
    void updateFranchiseName_shouldUpdateName() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.updateFranchiseName("franchise-1", "NewName"))
                .assertNext(f -> assertThat(f.getName()).isEqualTo("NewName"))
                .verifyComplete();
    }

    @Test
    @DisplayName("updateFranchiseName: should emit error when franchise not found")
    void updateFranchiseName_shouldErrorWhenNotFound() {
        when(franchiseRepository.findById("missing")).thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.updateFranchiseName("missing", "X"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Franchise not found"))
                .verify();
    }

    // ── addBranch ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addBranch: should add a branch to an existing franchise")
    void addBranch_shouldAddBranchToFranchise() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.addBranch("franchise-1", "Branch South"))
                .assertNext(f -> {
                    assertThat(f.getBranches()).hasSize(2);
                    assertThat(f.getBranches().get(1).getName()).isEqualTo("Branch South");
                })
                .verifyComplete();
    }

    // ── addProduct ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addProduct: should add a product to an existing branch")
    void addProduct_shouldAddProductToBranch() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.addProduct("franchise-1", "branch-1", "Fries", 50))
                .assertNext(f -> {
                    List<Product> products = f.getBranches().get(0).getProducts();
                    assertThat(products).hasSize(2);
                    assertThat(products.get(1).getName()).isEqualTo("Fries");
                    assertThat(products.get(1).getStock()).isEqualTo(50);
                })
                .verifyComplete();
    }

    // ── removeProduct ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeProduct: should remove a product from a branch")
    void removeProduct_shouldRemoveProduct() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.removeProduct("franchise-1", "branch-1", "prod-1"))
                .assertNext(f -> assertThat(f.getBranches().get(0).getProducts()).isEmpty())
                .verifyComplete();
    }

    // ── updateProductStock ────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProductStock: should update stock correctly")
    void updateProductStock_shouldUpdateStock() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.updateProductStock("franchise-1", "branch-1", "prod-1", 999))
                .assertNext(f -> assertThat(
                        f.getBranches().get(0).getProducts().get(0).getStock()).isEqualTo(999))
                .verifyComplete();
    }

    // ── updateProductName ─────────────────────────────────────────────────────

    @Test
    @DisplayName("updateProductName: should update product name correctly")
    void updateProductName_shouldUpdateName() {
        when(franchiseRepository.findById("franchise-1")).thenReturn(Mono.just(sampleFranchise));
        when(franchiseRepository.save(any())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(franchiseUseCase.updateProductName("franchise-1", "branch-1", "prod-1", "Whopper"))
                .assertNext(f -> assertThat(
                        f.getBranches().get(0).getProducts().get(0).getName()).isEqualTo("Whopper"))
                .verifyComplete();
    }

    // ── getTopStockProductsPerBranch ──────────────────────────────────────────

    @Test
    @DisplayName("getTopStockProductsPerBranch: should return the product with highest stock per branch")
    void getTopStockProducts_shouldReturnHighestStockPerBranch() {
        Product lowStock  = Product.builder().id("p2").name("Soda").stock(10).build();
        Product highStock = Product.builder().id("p3").name("Burger").stock(200).build();

        Branch branch2 = Branch.builder()
                .id("branch-2")
                .name("Branch South")
                .products(new ArrayList<>(List.of(lowStock, highStock)))
                .build();

        Franchise franchise = Franchise.builder()
                .id("f-1")
                .name("FastFood Inc")
                .branches(new ArrayList<>(List.of(sampleBranch, branch2)))
                .build();

        when(franchiseRepository.findById("f-1")).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseUseCase.getTopStockProductsPerBranch("f-1"))
                .assertNext(top -> {
                    assertThat(top.getBranchId()).isEqualTo("branch-1");
                    assertThat(top.getStock()).isEqualTo(100);
                })
                .assertNext(top -> {
                    assertThat(top.getBranchId()).isEqualTo("branch-2");
                    assertThat(top.getProductName()).isEqualTo("Burger");
                    assertThat(top.getStock()).isEqualTo(200);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getTopStockProductsPerBranch: should skip branches with no products")
    void getTopStockProducts_shouldSkipEmptyBranches() {
        Branch emptyBranch = Branch.builder()
                .id("branch-empty")
                .name("Empty Branch")
                .products(new ArrayList<>())
                .build();

        Franchise franchise = Franchise.builder()
                .id("f-2")
                .name("Sparse Co")
                .branches(new ArrayList<>(List.of(emptyBranch, sampleBranch)))
                .build();

        when(franchiseRepository.findById("f-2")).thenReturn(Mono.just(franchise));

        StepVerifier.create(franchiseUseCase.getTopStockProductsPerBranch("f-2"))
                .assertNext(top -> assertThat(top.getBranchId()).isEqualTo("branch-1"))
                .verifyComplete();
    }

    @Test
    @DisplayName("getTopStockProductsPerBranch: should emit error when franchise not found")
    void getTopStockProducts_shouldErrorWhenFranchiseNotFound() {
        when(franchiseRepository.findById("nope")).thenReturn(Mono.empty());

        StepVerifier.create(franchiseUseCase.getTopStockProductsPerBranch("nope"))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Franchise not found"))
                .verify();
    }
}