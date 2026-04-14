package com.accenture.franchiseapi.api.router;

import com.accenture.franchiseapi.api.handler.FranchiseHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class FranchiseRouter {

    private static final String BASE = "/api/v1";
    private static final String FRANCHISE = BASE + "/franchises";
    private static final String BRANCH = FRANCHISE + "/{franchiseId}/branches";
    private static final String PRODUCT = BRANCH + "/{branchId}/products";

    @Bean
    @RouterOperations({
            @RouterOperation(path = BASE + "/franchises", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addFranchise",
                    operation = @Operation(operationId = "addFranchise", summary = "Add a new franchise", tags = {"Franchise"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateFranchiseName",
                    operation = @Operation(operationId = "updateFranchiseName", summary = "Update franchise name", tags = {"Franchise"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addBranch",
                    operation = @Operation(operationId = "addBranch", summary = "Add branch to franchise", tags = {"Branch"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches/{branchId}", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateBranchName",
                    operation = @Operation(operationId = "updateBranchName", summary = "Update branch name", tags = {"Branch"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches/{branchId}/products", method = RequestMethod.POST,
                    beanClass = FranchiseHandler.class, beanMethod = "addProduct",
                    operation = @Operation(operationId = "addProduct", summary = "Add product to branch", tags = {"Product"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}", method = RequestMethod.DELETE,
                    beanClass = FranchiseHandler.class, beanMethod = "removeProduct",
                    operation = @Operation(operationId = "removeProduct", summary = "Remove product from branch", tags = {"Product"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}/stock", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductStock",
                    operation = @Operation(operationId = "updateProductStock", summary = "Update product stock", tags = {"Product"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/branches/{branchId}/products/{productId}/name", method = RequestMethod.PATCH,
                    beanClass = FranchiseHandler.class, beanMethod = "updateProductName",
                    operation = @Operation(operationId = "updateProductName", summary = "Update product name", tags = {"Product"})),
            @RouterOperation(path = BASE + "/franchises/{franchiseId}/top-stock-products", method = RequestMethod.GET,
                    beanClass = FranchiseHandler.class, beanMethod = "getTopStockProducts",
                    operation = @Operation(operationId = "getTopStockProducts", summary = "Get top-stock product per branch", tags = {"Franchise"}))
    })

    public RouterFunction<ServerResponse> franchiseRoutes(FranchiseHandler handler) {
        return RouterFunctions.route()
                // Franchises
                .POST(FRANCHISE, accept(MediaType.APPLICATION_JSON), handler::addFranchise)
                .PATCH(FRANCHISE + "/{franchiseId}", accept(MediaType.APPLICATION_JSON), handler::updateFranchiseName)
                // Branches
                .POST(BRANCH, accept(MediaType.APPLICATION_JSON), handler::addBranch)
                .PATCH(BRANCH + "/{branchId}", accept(MediaType.APPLICATION_JSON), handler::updateBranchName)
                // Products
                .POST(PRODUCT, accept(MediaType.APPLICATION_JSON), handler::addProduct)
                .DELETE(PRODUCT + "/{productId}", handler::removeProduct)
                .PATCH(PRODUCT + "/{productId}/stock", accept(MediaType.APPLICATION_JSON), handler::updateProductStock)
                .PATCH(PRODUCT + "/{productId}/name", accept(MediaType.APPLICATION_JSON), handler::updateProductName)
                // Query
                .GET(FRANCHISE + "/{franchiseId}/top-stock-products", handler::getTopStockProducts)
                .build();
    }
}