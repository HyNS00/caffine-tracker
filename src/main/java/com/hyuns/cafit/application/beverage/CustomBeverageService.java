package com.hyuns.cafit.application.beverage;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.repository.CustomBeverageRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageCreateRequest;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageResponse;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageUpdateRequest;
import com.hyuns.cafit.global.exception.BeverageAccessDeniedException;
import com.hyuns.cafit.global.exception.CustomBeverageNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomBeverageService {
    private final CustomBeverageRepository customBeverageRepository;

    @Transactional
    public CustomBeverageResponse createCustomBeverage(
            User user,
            CustomBeverageCreateRequest createRequest
    ){
        CustomBeverage beverage = CustomBeverage.create(
                user,
                createRequest.name(),
                createRequest.category(),
                createRequest.volumeMl(),
                createRequest.caffeineMg()
        );

        CustomBeverage saved = customBeverageRepository.save(beverage);
        return CustomBeverageResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CustomBeverageResponse> getMyCustomBeverages(User user) {
        List<CustomBeverage> beverages = customBeverageRepository.findByUser(user);

        return beverages.stream()
                .map(CustomBeverageResponse::from)
                .toList();
    }

    @Transactional
    public CustomBeverageResponse updateCustomBeverage(
            Long beverageId,
            User user,
            CustomBeverageUpdateRequest updateRequest
    ) {
        CustomBeverage beverage = findByIdOrThrow(beverageId);
        validateOwnership(beverage, user);

        beverage.update(
                updateRequest.name(),
                updateRequest.volumeMl(),
                updateRequest.caffeineMg()
        );

        return CustomBeverageResponse.from(beverage);
    }

    @Transactional
    public void deleteCustomBeverage(Long beverageId, User user) {
        CustomBeverage beverage = findByIdOrThrow(beverageId);

        // 권한 검증: 본인의 음료인지 확인
        validateOwnership(beverage, user);

        customBeverageRepository.delete(beverage);
    }

    @Transactional(readOnly = true)
    public CustomBeverage getById(Long id) {
        return customBeverageRepository.findById(id)
                .orElseThrow(CustomBeverageNotFoundException::new);
    }

    @Transactional(readOnly = true)
    public CustomBeverage getByIdAndValidateOwnership(Long id, User user) {
        CustomBeverage beverage = getById(id);
        validateOwnership(beverage, user);
        return beverage;
    }

    private CustomBeverage findByIdOrThrow(Long beverageId) {
        return customBeverageRepository.findById(beverageId)
                .orElseThrow(CustomBeverageNotFoundException::new);
    }

    private void validateOwnership(CustomBeverage beverage, User user) {
        if (!beverage.isOwnedBy(user)) {
            throw new BeverageAccessDeniedException();
        }
    }
}
