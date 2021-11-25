package com.artineer.spring_study_week2.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@DataJdbcTest
class ArticleRepositoryTest {
    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void findByTitle_title_조회하면_일치하는_Article_객체들이_반환된다() {
        //given
        String javaStub = "Java";
        String pythonStub = "Python";

        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(javaStub).build());
        articleRepository.save(Article.builder().title(pythonStub).build());

        //when
        List<Article> articles = articleRepository.findByTitle(javaStub);

        //then
        assertThat(articles.size()).isEqualTo(2);
        assertTrue(articles.stream().allMatch(a-> javaStub.equals(a.getTitle())),"title"+javaStub+"인가?");
    }
}