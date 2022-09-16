package com.example.newsapi.controller;

import com.example.newsapi.dto.LockAccountResponseDto;
import com.example.newsapi.model.LockAccount;
import com.example.newsapi.model.Views;
import com.example.newsapi.service.LockService;
import com.example.newsapi.util.BindingResultUtils;
import com.fasterxml.jackson.annotation.JsonView;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("api/lock")
@AllArgsConstructor
@NoArgsConstructor
public class LockController {
    @Autowired
    private LockService lockService;

    private final static HttpStatus STATUS_OK = HttpStatus.OK;
    private final static HttpStatus STATUS_BAD_REQUEST = HttpStatus.BAD_REQUEST;
    private final static HttpStatus STATUS_NOT_FOUND = HttpStatus.NOT_FOUND;

    @GetMapping("all")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(Views.ShortLockAccount.class)
    public ResponseEntity<List<LockAccount>> getAll() {
        return new ResponseEntity<>(lockService.getAll(), STATUS_OK);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(Views.FullLockAccount.class)
    public ResponseEntity<LockAccount> getOne(@RequestParam Long userId) {
        Optional<LockAccount> lockAccount = lockService.getByBannedUserId(userId);
        return lockAccount
                .map(account -> new ResponseEntity<>(account, STATUS_OK))
                .orElseGet(() -> new ResponseEntity<>(null, STATUS_NOT_FOUND));

    }

    @PostMapping("lock")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(Views.FullLockAccount.class)
    public ResponseEntity<LockAccountResponseDto> lockAccount(@RequestParam Long userId,
                                                              @RequestBody @Valid LockAccount requestLockAccount,
                                                              BindingResult bindingResult) {

        LockAccount lockAccount = lockService.lockAccount(requestLockAccount, userId, bindingResult);

        if (lockAccount == null) {
            return new ResponseEntity<>(
                    new LockAccountResponseDto(null, BindingResultUtils.getErrors(bindingResult)),
                    STATUS_BAD_REQUEST);
        }

        return new ResponseEntity<>(new LockAccountResponseDto(lockAccount, null), STATUS_OK);
    }

    @PostMapping("unlock")
    @PreAuthorize("hasAuthority('ADMIN')")
    @JsonView(Views.FullLockAccount.class)
    public ResponseEntity<LockAccountResponseDto> unlockAccount(@RequestParam Long userId) {
        Map<String, List<String>> errors = new HashMap<>();
        lockService.unlockAccount(userId, errors);

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(
                    new LockAccountResponseDto(null, errors),
                    STATUS_BAD_REQUEST);
        }

        return new ResponseEntity<>(STATUS_OK);
    }
}
