package com.artineer.spring_study_week2.service;

import com.artineer.spring_study_week2.domain.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleServiceProxy implements ArticleService {
    private final ArticleServiceImpl articleServiceImpl;

    @Override
    public Long save(Article request) {
        return articleServiceImpl.save(request);
    }

    @Override
    public Article findById(Long id) {
        log.info("Request : id - {}", id);

        Article article = articleServiceImpl.findById(id);

        log.info("Response : article.title - {}, article.content - {}", article.getTitle(), article.getContent());

        return article;
    }

    @Override
    public Article update(Article request) {
        return articleServiceImpl.update(request);
    }

    @Override
    public void delete(Long id) {
        articleServiceImpl.delete(id);
    }
}