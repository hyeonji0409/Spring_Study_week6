package com.artineer.spring_study_week2.service;

import com.artineer.spring_study_week2.domain.Article;
import com.artineer.spring_study_week2.domain.ArticleRepository;
import com.artineer.spring_study_week2.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    private ArticleService articleService;

    @Mock
    private ArticleRepository articleRepository;

    @Test
    public void findById_id_에_맞는_Article_객체를_가져온다() {
        //given
        Long idStub = anyLong();
        Optional<Article> articleOptionStub = Optional.of(Article.builder()
                .id(eq(idStub)).title(anyString()).content(anyString()).build());

        when(articleRepository.findById(eq(idStub))).thenReturn(articleOptionStub);

        //when
        Article result = articleService.findById(idStub);

        //then
        assertThat(result.getId()).isEqualTo(articleOptionStub.get().getId());

    }

    @Test
    private void findById_id_에_맞는_Article_객체가_없다면_DATA_IS_NOT_FOUND_오류발생() {
        //given
        Long idStub = 1L;

        //when
        when(articleRepository.findById(idStub)).thenReturn(Optional.empty());

        //then
        assertThatThrownBy(()-> articleService.findById(idStub))
                .isInstanceOf(ApiException.class)
                .hasMessage("article is not found");
    }
}