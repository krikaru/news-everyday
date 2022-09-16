package com.example.newsapi.controller;

import com.example.newsapi.model.LockAccount;
import com.example.newsapi.service.AuthorizationService;
import com.example.newsapi.service.LockService;
import com.example.newsapi.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class LockControllerTest {
    static final String LOCK_URL = "/api/lock";
    static final String CHARACTER_ENCODING = "utf-8";
    static final LockAccount lockAccount = LockAccount.builder().id(1L).reason("Reason of lock").build();

    @MockBean
    AuthorizationService authorizationService;
    @MockBean
    LockService lockService;
    @MockBean
    UserService userService;

    LockController lockController;
    BindingResult bindingResult;

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        lockController = spy(new LockController());
        bindingResult = new BeanPropertyBindingResult(new Object(), "any");
        objectMapper = new ObjectMapper();
        lockAccount.setBannedUser(null);
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void getOne_ifLockExist() {
        Optional<LockAccount> lockOptional = Optional.of(lockAccount);
        when(lockService.getByBannedUserId(any())).thenReturn(lockOptional);

        mockMvc.perform(get(LOCK_URL + "?userId=1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(1L), Long.class));
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void getOne_ifLockNotExist() {
        when(lockService.getByBannedUserId(any())).thenReturn(Optional.empty());

        mockMvc.perform(get(LOCK_URL + "?userId=1")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_return400IfFailureLock() {
        when(lockService.lockAccount(any(), any(), any())).thenReturn(null);

        mockMvc.perform(post(LOCK_URL +"/lock?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lockAccount))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.lockAccount").doesNotExist());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void lockAccount_return200IfSuccessLock() {
        when(lockService.lockAccount(any(), any(), any())).thenReturn(lockAccount);

        mockMvc.perform(post(LOCK_URL +"/lock?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(lockAccount))
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lockAccount").isNotEmpty())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void unlockAccount_return200IfSuccessUnlock() {
        doNothing().when(lockService).unlockAccount(any(), any());

        mockMvc.perform(post(LOCK_URL +"/unlock?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk());
    }


    @Test
    @SneakyThrows
    @WithMockUser(authorities = {"ADMIN"})
    void unlockAccount_return400IfFailureUnlock() {
        doNothing().when(lockService).unlockAccount(any(), any());
        Map mock = mock(Map.class);
        lenient().when(mock.isEmpty()).thenReturn(false);

        mockMvc.perform(post(LOCK_URL +"/unlock?userId=1")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding(CHARACTER_ENCODING))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").doesNotExist());
    }
}