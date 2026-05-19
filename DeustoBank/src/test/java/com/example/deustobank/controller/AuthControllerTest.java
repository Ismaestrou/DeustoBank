package com.example.deustobank.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.deustobank.model.User;
import com.example.deustobank.service.AuthService;
import com.example.deustobank.service.TokenBlacklistService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private TokenBlacklistService tokenBlacklist;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;

    @BeforeEach
    void setUp() {

        testUser = new User();
        testUser.setId(1L);
        testUser.setDni("12345678A");
        testUser.setEmail("test@test.com");
    }

    @Test
    void register_Success() throws Exception {

        when(authService.register(any(User.class)))
                .thenReturn(testUser);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.dni").value("12345678A"));
    }

    @Test
    void register_BadRequest() throws Exception {

        when(authService.register(any(User.class)))
                .thenThrow(new RuntimeException("Error register"));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUser)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Error register"));
    }

    @Test
    void login_Success() throws Exception {

        when(authService.login("12345678A", "pass"))
                .thenReturn(testUser);

        mockMvc.perform(post("/auth/login")
                        .param("dni", "12345678A")
                        .param("password", "pass"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void login_Unauthorized() throws Exception {

        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Bad credentials"));

        mockMvc.perform(post("/auth/login")
                        .param("dni", "12345678A")
                        .param("password", "pass"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Bad credentials"));
    }

    @Test
    void updateProfile_Success() throws Exception {

        when(authService.updateProfile(
                eq(1L),
                anyString(),
                anyString()))
                .thenReturn(testUser);

        mockMvc.perform(put("/auth/update/1")
                        .param("email", "new@test.com")
                        .param("phone", "123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateProfile_Error() throws Exception {

        when(authService.updateProfile(
                eq(1L),
                anyString(),
                anyString()))
                .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(put("/auth/update/1")
                        .param("email", "new@test.com")
                        .param("phone", "123456"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Update failed"));
    }

    @Test
    void logout_Success() throws Exception {

        mockMvc.perform(post("/auth/logout")
                        .param("sessionToken", "some-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                .value("Sesión cerrada correctamente"));
    }

    // ============================================================
    // logout – sin token (rama sessionToken == null / blank)
    // ============================================================

    @Test
    void logout_NoToken() throws Exception {

        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                .value("Sesión cerrada correctamente"));
    }

    // ============================================================
    // changePassword – éxito
    // ============================================================

    @Test
    void changePassword_Success() throws Exception {

        org.mockito.Mockito.doNothing()
                .when(authService)
                .changePassword(1L, "oldPass", "newPass");

        mockMvc.perform(put("/auth/change-password/1")
                        .param("passwordActual", "oldPass")
                        .param("passwordNueva", "newPass"))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña cambiada correctamente"));
    }

    // ============================================================
    // changePassword – contraseña incorrecta (rama catch)
    // ============================================================

    @Test
    void changePassword_WrongPassword() throws Exception {

        org.mockito.Mockito.doThrow(new RuntimeException("Contraseña actual incorrecta"))
                .when(authService)
                .changePassword(1L, "wrongPass", "newPass");

        mockMvc.perform(put("/auth/change-password/1")
                        .param("passwordActual", "wrongPass")
                        .param("passwordNueva", "newPass"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Contraseña actual incorrecta"));
    }
}