package com.example.newsapi.service;

import com.example.newsapi.controller.LockController;
import com.example.newsapi.model.AppUser;
import com.example.newsapi.model.LockAccount;
import com.example.newsapi.repo.LockRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LockServiceTest {

    static final LockAccount lockAccount = LockAccount.builder().id(1L).reason("Reason of lock").build();
    static final AppUser principal = AppUser.builder().id(1L).email("test1").build();
    static final AppUser bannedUser = AppUser.builder().id(2L).email("test2").build();

    @Mock
    AuthorizationService authorizationService;
    LockService lockService;
    @Mock
    UserService userService;
    @Mock
    LockRepo lockRepo;

    LockController lockController;
    BindingResult bindingResult;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        lockService = spy(new LockService(lockRepo, userService, authorizationService));
        lockController = spy(new LockController());
        bindingResult = new BeanPropertyBindingResult(new Object(), "any");
        objectMapper = new ObjectMapper();
        lockAccount.setBannedUser(null);
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_ifBannedUserIsNotExist() {
        when(userService.findById(any())).thenReturn(Optional.empty());
        when(authorizationService.getPrincipal()).thenReturn(principal);

        LockAccount lockAccount = lockService.lockAccount(LockServiceTest.lockAccount, 2L, bindingResult);

        assertThat(lockAccount).isNull();
        verify(lockRepo, never()).save(any());
        assertThat(bindingResult.getAllErrors()).isNotEmpty();
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_ifBannedUserAlreadyHasLock() {
        when(userService.findById(any())).thenReturn(Optional.of(bannedUser));
        when(authorizationService.getPrincipal()).thenReturn(principal);
        doReturn(true).when(lockService).existByBannedUser(any());

        LockAccount lockAccount = lockService.lockAccount(LockServiceTest.lockAccount, 2L, bindingResult);

        assertThat(lockAccount).isNull();
        verify(lockRepo, never()).save(any());
        assertThat(bindingResult.getAllErrors()).isNotEmpty();
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_ifBannedUserIsPrincipal() {
        when(userService.findById(any())).thenReturn(Optional.of(principal));
        when(authorizationService.getPrincipal()).thenReturn(principal);

        LockAccount lockAccount = lockService.lockAccount(LockServiceTest.lockAccount, 2L, bindingResult);

        assertThat(lockAccount).isNull();
        verify(lockRepo, never()).save(any());
        assertThat(bindingResult.getAllErrors()).isNotEmpty();
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_ifBannedUserValid() {
        when(userService.findById(any())).thenReturn(Optional.of(bannedUser));
        when(authorizationService.getPrincipal()).thenReturn(principal);
        doReturn(false).when(lockService).existByBannedUser(any());

        lockService.lockAccount(LockServiceTest.lockAccount, 2L, bindingResult);

        verify(lockRepo).save(any());
        assertThat(bindingResult.getAllErrors()).isEmpty();
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void unlockAccount_ifLockIsNotExist() {
        when(lockRepo.getByBannedUserId(any())).thenReturn(Optional.empty());
        when(authorizationService.getPrincipal()).thenReturn(principal);
        HashMap<String, List<String>> errors = new HashMap<>();

        lockService.unlockAccount(2L, errors);

        assertThat(errors).isNotNull();
        verify(lockRepo, never()).deleteById(anyLong());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void unlockAccount_ifBannedUserIsPrincipal() {
        lockAccount.setBannedUser(principal);
        when(lockRepo.getByBannedUserId(any())).thenReturn(Optional.of(lockAccount));
        when(authorizationService.getPrincipal()).thenReturn(principal);
        HashMap<String, List<String>> errors = new HashMap<>();

        lockService.unlockAccount(1L, errors);

        assertThat(errors).isNotNull();
        verify(lockRepo, never()).deleteById(anyLong());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void unlockAccount_ifLockIsExist() {
        when(lockRepo.getByBannedUserId(any())).thenReturn(Optional.of(lockAccount));
        when(authorizationService.getPrincipal()).thenReturn(principal);
        HashMap<String, List<String>> errors = new HashMap<>();

        lockService.unlockAccount(2L, errors);

        assertThat(errors).isEmpty();
        verify(lockRepo).deleteById(anyLong());
    }
}