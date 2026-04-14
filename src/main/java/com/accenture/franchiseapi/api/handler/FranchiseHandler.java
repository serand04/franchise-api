package com.accenture.franchiseapi.api.handler;

import com.accenture.franchiseapi.domain.port.in.FranchiseUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import static com.accenture.franchiseapi.api.handler.FranchiseDtos.*;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;
    private final Validator validator;

    // ── Franchises ───────────────────────────────────────────────────────────

    public Mono<ServerResponse> addFranchise(ServerRequest request) {
        return request.bodyToMono(CreateFranchiseRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.addFranchise(body.getName()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise))
                .onErrorResume(ResponseStatusException.class, e ->
                        ServerResponse.status(e.getStatusCode()).bodyValue(e.getReason()));
    }

    public Mono<ServerResponse> updateFranchiseName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(UpdateNameRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.updateFranchiseName(franchiseId, body.getName()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    // ── Branches ─────────────────────────────────────────────────────────────

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return request.bodyToMono(AddBranchRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.addBranch(franchiseId, body.getName()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> updateBranchName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId    = request.pathVariable("branchId");
        return request.bodyToMono(UpdateNameRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.updateBranchName(franchiseId, branchId, body.getName()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    // ── Products ─────────────────────────────────────────────────────────────

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId    = request.pathVariable("branchId");
        return request.bodyToMono(AddProductRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.addProduct(franchiseId, branchId, body.getName(), body.getStock()))
                .flatMap(franchise -> ServerResponse.status(HttpStatus.CREATED).bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId    = request.pathVariable("branchId");
        String productId   = request.pathVariable("productId");
        return franchiseUseCase.removeProduct(franchiseId, branchId, productId)
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> updateProductStock(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId    = request.pathVariable("branchId");
        String productId   = request.pathVariable("productId");
        return request.bodyToMono(UpdateStockRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.updateProductStock(franchiseId, branchId, productId, body.getStock()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    public Mono<ServerResponse> updateProductName(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        String branchId    = request.pathVariable("branchId");
        String productId   = request.pathVariable("productId");
        return request.bodyToMono(UpdateNameRequest.class)
                .doOnNext(this::validate)
                .flatMap(body -> franchiseUseCase.updateProductName(franchiseId, branchId, productId, body.getName()))
                .flatMap(franchise -> ServerResponse.ok().bodyValue(franchise))
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    // ── Queries ──────────────────────────────────────────────────────────────

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        String franchiseId = request.pathVariable("franchiseId");
        return ServerResponse.ok()
                .body(franchiseUseCase.getTopStockProductsPerBranch(franchiseId),
                        com.accenture.franchiseapi.domain.model.TopStockProduct.class)
                .onErrorResume(RuntimeException.class, e ->
                        ServerResponse.status(HttpStatus.NOT_FOUND).bodyValue(e.getMessage()));
    }

    // ── Validation helper ────────────────────────────────────────────────────

    private <T> void validate(T body) {
        var errors = new BeanPropertyBindingResult(body, body.getClass().getSimpleName());
        validator.validate(body, errors);
        if (errors.hasErrors()) {
            String message = errors.getAllErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Validation error");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }
}
