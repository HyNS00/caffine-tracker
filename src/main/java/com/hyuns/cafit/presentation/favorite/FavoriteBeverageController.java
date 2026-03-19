package com.hyuns.cafit.presentation.favorite;

import com.hyuns.cafit.global.security.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.favorite.FavoriteBeverageResponse;
import com.hyuns.cafit.dto.favorite.FavoriteCreateRequest;
import com.hyuns.cafit.dto.favorite.FavoriteOrderUpdateRequest;
import com.hyuns.cafit.application.favorite.FavoriteBeverageFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteBeverageController {
    private final FavoriteBeverageFacade favoriteFacade;

    @PostMapping
    public ResponseEntity<FavoriteBeverageResponse> addFavorite(
            @Login User user,
            @Valid @RequestBody FavoriteCreateRequest request
    ) {
        return ResponseEntity.ok(favoriteFacade.addFavorite(user, request));
    }

    @GetMapping
    public ResponseEntity<List<FavoriteBeverageResponse>> getFavorites(
            @Login User user
    ) {
        return ResponseEntity.ok(favoriteFacade.getFavorites(user));
    }

    @DeleteMapping("/{favoriteId}")
    public ResponseEntity<Void> deleteFavorite(
            @PathVariable Long favoriteId,
            @Login User user
    ) {
        favoriteFacade.deleteFavorite(favoriteId, user);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/order")
    public ResponseEntity<Void> updateOrder(
            @Login User user,
            @Valid @RequestBody FavoriteOrderUpdateRequest request
    ) {
        favoriteFacade.updateOrder(user, request.favoriteIds());
        return ResponseEntity.ok().build();
    }
}
