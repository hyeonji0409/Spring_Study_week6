package com.artineer.spring_study_week2.service;

import com.artineer.spring_study_week2.aspect.ExecuteLog;
import com.artineer.spring_study_week2.domain.Article;
import com.artineer.spring_study_week2.domain.ArticleRepository;
import com.artineer.spring_study_week2.exception.ApiException;
import com.artineer.spring_study_week2.exception.Asserts;
import com.artineer.spring_study_week2.vo.ApiCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Primary
@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;

    @Override
    public Long save(Article request) {
        return articleRepository.save(request).getId();
    }

    @ExecuteLog(type = Article.class)
    @Override
    public Article findById(Long id) {   return articleRepository.findById(id)
            .orElseThrow(() -> new ApiException(ApiCode.DATA_IS_NOT_FOUND, "article is not found"));
    }


    @Override
    @Transactional
    public Article update(Article request) {
        Article article = this.findById(request.getId());
        article.update(request.getTitle(), request.getContent());

        return article;
    }

    @Override
    public void delete(Long id) {
        articleRepository.deleteById(id);
    }
}