package com.artineer.spring_study_week2.controller;

import com.artineer.spring_study_week2.domain.ArticleRepository;
import com.artineer.spring_study_week2.dto.ArticleDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ArticleIntegrationTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ArticleRepository articleRepository;

    // API 테스트를 할 때, 요청하는 클라이언트를 대신해주는 역할
    // API call을 해서 잘 동작하는지 확인
    private MockMvc mvc;

    // 각 테스트 실행 이전에 설정을 하기 위한 함수(초기 설정 함수)
    @BeforeEach
    public void setup(){
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    @DisplayName("post() 실행되면 article 객체가 새로 생성되어야 한다.")
    public void post_whenItIsOccured_thenArticleShouldbeStored() throws Exception {
        //given
        ArticleDto.ReqPost requestBodyStub = ArticleDto.ReqPost.builder()
                .title("title")
                .content("content")
                .build();

        //when
        mvc.perform(post("/api/v1/artile")
                .characterEncoding("utf-8")
                .accept(MediaType.APPLICATION_JSON) // client가 서버에 미리 얘기해주는 것. 어떤 타입을 받을 수 있는지
                .contentType(MediaType.APPLICATION_JSON) // 실제로 그 타입을 받아와야만 함. 하나의 값만 넣을 수 있음
                .content(new ObjectMapper().writeValueAsString(requestBodyStub))
        )
                .andDo(print())
                .andExpect(status().isOk()); // 하나의 Test의 역할을 함

        //then
        // 실제 db의 객체 갯수 받아오기
        long size = articleRepository.count();
        assertThat(size).isEqualTo(1);

    }
}
