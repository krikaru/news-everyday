package com.example.newsapi.service;

import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.LockAccount;
import com.example.newsapi.repo.LockRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.newsapi.util.BindingResultUtils.addErrors;

@Service
@NoArgsConstructor
@AllArgsConstructor
public class LockService {
    @Autowired
    private LockRepo lockRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthorizationService authorizationService;

    private final static String INVALID_FIELD_NAME_ID = "id";

    public void unlockAccount(Long userId, Map<String, List<String>> errors) {
        Optional<LockAccount> lockOptional = lockRepo.getByBannedUserId(userId);
        AppUser principal = authorizationService.getPrincipal();

        if (lockOptional.isPresent()) {
            AppUser bannedUser = lockOptional.get().getBannedUser();
            if (principal.equals(bannedUser)) {
                errors.put(INVALID_FIELD_NAME_ID, List.of("Нельзя разбанить самого себя!"));
            }

            if (errors.isEmpty()) {
                lockRepo.deleteById(lockOptional.get().getId());
            }
        } else {
            errors.put(INVALID_FIELD_NAME_ID, List.of("Пользователь не забанен или не существует!"));
        }
    }

    public LockAccount lockAccount(LockAccount lockAccount, Long userId, BindingResult bindingResult) {
        AppUser bannedUser = userService.findById(userId).orElse(null);
        AppUser principal = authorizationService.getPrincipal();

        if (principal.equals(bannedUser)) addErrors(
                bindingResult, INVALID_FIELD_NAME_ID, "Нельзя забанить самого себя!");

        if (bannedUser == null) {
            addErrors(bindingResult, INVALID_FIELD_NAME_ID, "Пользователя с таким id не существует!");
        } else {
            boolean lockIsExist = existByBannedUser(bannedUser);
            if (lockIsExist) addErrors(
                    bindingResult, INVALID_FIELD_NAME_ID, "Пользователь уже забанен!");

        }

        if (bindingResult.hasErrors()) {
            return null;
        }

        lockAccount.setBanDate(LocalDateTime.now());
        lockAccount.setBannedUser(bannedUser);
        lockAccount.setModeratorUser(principal);
        return lockRepo.save(lockAccount);
    }

    public List<LockAccount> getAll() {
        return lockRepo.findAll();
    }

    public Optional<LockAccount> getByBannedUserId(Long userId) {
        return lockRepo.getByBannedUserId(userId);
    }

    public boolean existByBannedUser(AppUser bannedUser) {
        return lockRepo.existsByBannedUser(bannedUser);
    }
}
