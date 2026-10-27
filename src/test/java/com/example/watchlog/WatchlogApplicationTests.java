package com.example.watchlog;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WatchlogApplicationTests {
    private final MockMvc mockMvc;

    @Autowired
    WatchlogApplicationTests(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void contextLoads() {}

    @Test
    void userCanRegisterAndCreateViewingHistory() throws Exception {
        MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "テストユーザー",
                                  "email": "integration@example.com",
                                  "password": "test-password-123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@example.com"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(post("/api/users/1/viewing-histories")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workId": 1,
                                  "status": "WATCHED",
                                  "watchedAt": "2026-10-27",
                                  "rating": 5,
                                  "comment": "統合テストで登録"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("WATCHED"))
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(get("/api/users/1/viewing-histories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].work.title").isNotEmpty());
    }

    @Test
    void viewingHistoryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/1/viewing-histories"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void invalidWorkReturnsUnifiedValidationError() throws Exception {
        mockMvc.perform(post("/api/works")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "description": "",
                                  "type": "MOVIE",
                                  "releaseYear": 99,
                                  "genre": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
