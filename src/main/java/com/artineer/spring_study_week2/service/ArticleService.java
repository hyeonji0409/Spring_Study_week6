package com.artineer.spring_study_week2.service;


import com.artineer.spring_study_week2.domain.Article;

public interface ArticleService {
    Long save(Article request);

    Article findById(Long id);

    Article update(Article request);

    void delete(Long id);
}