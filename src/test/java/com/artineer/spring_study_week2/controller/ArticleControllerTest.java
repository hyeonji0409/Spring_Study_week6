package com.artineer.spring_study_week2.controller;

import com.artineer.spring_study_week2.domain.Article;
import com.artineer.spring_study_week2.service.ArticleService;
import com.artineer.spring_study_week2.vo.ApiCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
class ArticleControllerTest {
    @MockBean
    ArticleService articleService;

    @Autowired
    MockMvc mvc;

    @Test
    @DisplayName("Response.ok() 함수 호출 시 code 값이 ApiCode.SUCCESS 값을 가져야 한다.")
    public void get_whenYouCallOk_ThenReturnSuccessCode() throws Exception {
        //given
        Long idStub = 1L;

        when(articleService.findById(idStub)).thenReturn(Article.builder().id(idStub).build());

        // when
        String response = mvc.perform(get("/api/v1/article/" + idStub)
                        .characterEncoding("utf-8")
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // then
        assertThat(ApiCode.SUCESS.getName()).isSubstringOf(response);
    }
}