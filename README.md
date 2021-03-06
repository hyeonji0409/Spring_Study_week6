# Spring Study Week6

.1. Logging



## 로깅을 왜 남기는가?

- 운영되고 있는 시스템에서 로깅을 하는 행위 = 과거에 통신한 데이터들의 흐름을 쫓을 수 있는 유일한 수단

- 시스템에 오류가 생겨서 고객이 문의하는 상황이라면?

    - 개발자의 입장에서 로그를 확인하는 방법밖에 없다.

    - 어떤 문제가 있는지 원인 파악이 중요



## 로그를 어떻게 관리할 것인가?

- 로그가 쌓이면 엄청난 정보가 되기 때문에 꼭 필요한 로그만을 사용해야 한다. 하지만, 필요한 정보만 추출하기 쉽지 않음
    - 다양한 모니터링 툴이 있다. (앞으로 차차 알아가기)



## 로깅을 어떻게 남기는 것이 좋은가?

- API의 맨 처음 요청이 들어온 값과 마지막 응답값을 남겨주는 것
    - 어떤 고객이 뭔가 이상하다고 확인해달라고 하면 입력값과, 그 결과로 인한 반환값을 봄으로써 어떤 이슈가 있는지 큰 틀 확인 가능 (가장 기본적이지만 중요한 기능)



## 로깅 작업을 위해 스프링에서 어떤 작업을 해야되는가??

### 1. 가장 심플한 로깅

- 조회를 통한 로깅 방법
- 로그 내역이 따로 나오기 때문에 여러 개의 요청이 들어오면 헷갈릴 수 있음
    - 합쳐서 로깅해도 무방
- article을 조회하는 기능과 로깅을 하는 기능이 같이 있기 때문에 주 목적인 article을 조회하는 데에 집중을 하지 못하는 코드가 됨



#### ArticleService.java

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleService {
    
    // ...

  public Article findById(Long id) {
    log.info("Request : id - {}", id); // 요청값

    Article article = articleRepository.findById(id)
            .orElseThrow(() -> new ApiException(ApiCode.DATA_IS_NOT_FOUND, "article is not found"));

    log.info("Response : article.title - {}, article.content - {}", article.getTitle(), article.getContent()); // 응답 값

    return article;
  }
  
  // ...
}
```

- 카탈리나 로그: 로그 파일을 남겨주는 것

- 로그 레벨을 남겨줄 수는 없음
    - 로그 레벨을 나눠야 모니터링 툴에서 정보를 유연하게 볼 수 있으나, println은 그 기능을 가지지 않음
    - Rombok을 가져왔기 때문에 @Slf4j를 쓰면 debug, error 등을 넣을 수 있음 -> 서버 로그에 남는다.
- @Slf4j
    - log4j 등을 추상화 시켜서 만든 어노테이션
    - 어떤 구현체(logger, log4j)를 쓰던지 동일한 formation의 log를 남길 수 있도록 해줌



### 2. Proxy Pattern을 활용한 로깅

- 스프링에서는 대부분 Proxy Pattern으로 구현된 AOP 사용
- 로그를 하는 코드와 비즈니스를 하는 코드를 분할하는 것이 목표
- 응집도를 높이고 결합도 낮출 수 있음
- 분리 가능한데 왜 한 곳에 두는가?
    - 두 기능의 의존성을 높이고, 결합도를 낮추기 위함



#### ArticleService -> ArticleServiceImpl

- 인터페이스에서는 보통 함수를 구현하지 않고 선언만 시행
- 함수의 시그니처: 함수가 반환하는 리턴 타입의 함수 이름(함수가 받는 파라미터 타입)



#### ArticleService 인터페이스 생성

```java
public interface ArticleService {
    // CRUD에 해당하는 시그니처
    Long save(Article request);

    Article findById(Long id);

    Article update(Article request);

    void delete(Long id);
}
```



#### ArticleServiceImpl (ArticleService 인터페이스를 구현)

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleServiceImpl implements ArticleService {
  private final ArticleRepository articleRepository;

  @Override
  public Long save(Article request) {
    return articleRepository.save(request).getId();
  }

  @Override
  public Article findById(Long id) {
    return articleRepository.findById(id)
            .orElseThrow(() -> new ApiException(ApiCode.DATA_IS_NOT_FOUND, "article is not found"));
  }

  @Transactional
  @Override
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
```

- ArticleService의 규정을 준수하는 또 다른 구현체 생성
- ArticleServiceInpl이 원본
- 원본과 Proxy가 같은 규정을 지키는 객체 => 인터페이스
- Proxy 객체는 원본 구현체를 감싸는 형태. 구현체를 그냥 호출해서 실행시키는 구조



#### 추가 내용

@ToString 지양 이유?

- 필드가 가진 값 모두 확인 가능하게 노출시킴
    - Article이라는 애가 something을 가지고있다고 가정.
        - Something도 Article을 가진다면?
            - 서로가 서로를 참조하는 상황 발생
            - 재귀 호출인 상황



#### ArticleServiceProxy

```java
@RequiredArgsConstructor // 의존성 주입을 위한 생성자 주입
@Service // 빈에 등록
public class ArticleServiceProxy implements ArticleService {
    private final ArticleServiceImpl articleServiceimpl;

    @Override
    public Long save(Article request) {
        return articleServiceimpl.save(request);
    }

    @Override
    public Article findById(Long id) {
        return articleServiceimpl.findById(id);
    }

    @Override
    public Article update(Article request) {
        return articleServiceimpl.update(request);
    }

    @Override
    public void delete(Long id) {
        articleServiceimpl.delete(id);
    }
}

```

- 구현체 실행 전, 실행 후에 원하는 작업 넣을 수 있음
- 로깅 작업 추가



#### ArticleServiceProxy

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleServiceProxy implements ArticleService {
  private final ArticleService articleService;

  @Override
  public Long save(Article request) {
    return articleService.save(request);
  }

  @Override
  public Article findById(Long id) {
    // Pre-Process
    log.info("Request : id - {}", id);

    Article article = articleService.findById(id);

    // Post-Process
    log.info("Response : article.title - {}, article.content - {}", article.getTitle(), article.getContent());

    return article;
  }

  @Override
  public Article update(Article request) {
    return articleService.update(request);
  }

  @Override
  public void delete(Long id) {
    articleService.delete(id);
  }
}
```

- 객체지향에서의 느슨한 결합 위해서는 구현체에 직접 접근하는 것보다 사이에 인터페이스를 두고 통신할 수 있도록 하는 것이 중요
- ArticleController는 ArticleService를 사용하는 클라이언트 입장에서 ArticleServiceImpl이나 ArticleServiceProxy에 대한 정보 필요 X
    - ArticleService 인터페이스와 소통
- 서버의 입장에서 프록시 객체를 만들어도 클라이언트는 내용을 알 필요가 없다. 반대로, 서버는 인터페이스 규격만 맞춘다면 원하는대로 개발 진행 가능

```
Parameter 0 of constructor in com.artineer.spring_lecture_week_2.conroller.ArticleController required a single bean, but 2 were found:
	- articleServiceImpl: defined in file [/Users/gobyeongchae/Desktop/Artineer_Spring/spring_lecture_week_6/build/classes/java/main/com/artineer/spring_lecture_week_2/service/ArticleServiceImpl.class]
	- articleServiceProxy: defined in file [/Users/gobyeongchae/Desktop/Artineer_Spring/spring_lecture_week_6/build/classes/java/main/com/artineer/spring_lecture_week_2/service/ArticleServiceProxy.class]
```

- ArticleController는 ArticleService를 구현하는 빈이 2개가 발견되어 오류 발생
    - Impl, Proxy 모두 ArticleService 구현하기 때문
    - @Primary 어노테이션을 사용하여 해결
        - 원본보다 우선순위가 높아져 최우선순위를 갖게함



#### ArticleController

```java
@Slf4j
@RequiredArgsConstructor
@Primary
@Service
public class ArticleServiceProxy implements ArticleService {
    // ...
}
```



#### 최종

- 인터페이스 설계하여 느슨한 결합을 가짐
- 클라이언트는 서버가 어떻게 구현되어있는지 모르고, 서버는 클라이언트에게 알려줄 필요 없음
- 프록시 객체에서 비즈니스 로직 제외 부가적 기능 처리하면 효과적으로 코드 분리가 가능하며, 원하는 기능을 구현할 수 있다.



### 3. AOP를 활용한 로깅

https://engkimbs.tistory.com/746 (참고해보기!!)

- AOP는 객체 중심이 아닌 기능 중심으로 서비스 제공
- AOP는 Proxy Pattern을 활용해 구현됨
- AOP는 다양한 형태로 구현될 수 있어 모두가 프록시 패턴으로 구현된다 오해하지 말것!!

- AOP 용어
    - Aspect: AOP(Aspect Oriented Programming) 객체로서 사용하겠다 명시
    - Advice:: 실제 모듈로서 동작되는 그 일 자체. 여리서는 로깅하는 업무 그 자체를 개념적으로 의미
    - Weaving: 프록시 객체에 원본을 끼워넣는 것
    - Around: 언제 Aspect를 Weaving 시킬 것인지 그 조건을 명시
    - JoinPoint: Advice가 적용된 위치
- AOP를 통한 로깅 구현
    - Around에 조건을 넣어 사용하는데 특정 어노테이션을 넣어서 어노테이션이 선언됐을 때 Aspect가 실행되도록 구현
    - Aspect 패키지 생성
        - Aspect 패키지에 ExecuteLog.java 어노테이션 생성



#### ExecuteLog

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteLog {
}
```

- @Target 어노테이션이 위치할 수 있는 곳 지정
    - 현재는 Method 위에만 지정 가능
- @Retention 어노테이션은 해당 어노테이션이 어느 시점까지 메모리에 존재하게 하는지 지정



#### ExecuteLogAspect

``` java
@Slf4j // 로깅을 하기 위한 로깅 라이브러리
@Component 
@Aspect // 흩어진 기능을 뭉쳐서 사용. AOP로 동작
public class ExecuteLogAspect {
  // 언제 실행되는지(위빙되는 시점)
  // @annotation(ExecuteLog)가 선언된 함수에서만 위빙이 일어난다.
  @Around(value = "@annotation(ExecuteLog)") 
  public Object log(ProceedingJoinPoint joinPoint) throws Throwable {
    
    long start = System.currentTimeMillis();

    final Object result = joinPoint.proceed(); 

    MethodSignature signature = (MethodSignature) joinPoint.getSignature(); 

    String methodName = signature.getName(); 
        String input = Arrays.toString(signature.getParameterNames()); // 입력에 해당하는 값

    String output = result.toString();

    log.info("Method Name : {}, Input : {}, Output : {}, Execute Time : {}", methodName, input, output, (System.currentTimeMillis() - start) + " ms");

    return result;
  }
}
```

#### ArticleServiceImpl

```java
@Slf4j
@RequiredArgsConstructor
@Service
public class ArticleServiceImpl implements ArticleService {
    
    // ...
  
    @ExecuteLog
    @Override
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ApiCode.DATA_IS_NOT_FOUND, "article is not found"));
    }
    
    // ...
}
```

- Proxy 쓰지 않기 위해 @Primary를 Impl로 이동

```java
@Slf4j
@RequiredArgsConstructor
@Primary
@Service
public class ArticleServiceImpl implements ArticleService {
  // ...
}
```

- 실행결과 확인하면 Output 부분이 세부적으로 나오지 않고 클래스 명과 해쉬코드 값 나옴
- toString( )을 오버라이딩 하지 않았기 때문에 자바에서 기본으로 제공되는 toString() 함수가 호출되는 것
- @ToString으로 해결 가능
    - 해결 가능하나, 실무에서는 지양됨.
    - 무한 재귀 호출로 StackOverFlowError 발생가능성 있기 때문

#### Article

```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
public class Article {
    // ...
}
```



### Optional) 4. Reflection 활용하여 어노텡션으로 정보 가져오기

- AOP같은 영역에 특정 정보를 전달해야 할 때 어노테이션으로 전달하는 방법이 가장 무난
- type 정보를 어노테이션으로 전달해서 output을 @ToString 없이 전달 가능하게 해보자!
- 기존 자바와는 다름
    - 일반적인 코딩은 코드를 짜기 -> 클래스 파일 생성
        - => 클래스 파일이 실행되고 런타임 상에서 동작
    - 중간 언어가 있는 언어는 Reflection이라는 테크닉 사용 가능
        - Reflection: 소스코드를 짤 때 소스코드가 클래스 코드로 가는 것이 원래 방향이지만, 소스코드를 짤 때 이미 만들어진 클래스 형식 가져와 사용

#### ArticleServiceImpl

```java
// ...
public class ArticleServiceImpl implements ArticleService {

    // ...
    
    @ExecuteLog(type = Article.class)
    @Override
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new ApiException(ApiCode.DATA_IS_NOT_FOUND, "article is not found"));
    }
    
    // ...
}
```

#### ExecuteLog

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExecuteLog {
    Class<?> type();
}
```

#### ExecuteLogAspect

```java
@Slf4j
@Component
@Aspect
public class ExecuteLogAspect {
    @SuppressWarnings("unchecked")
    @Around(value = "@annotation(ExecuteLog)")
    public <T> Object log(ProceedingJoinPoint joinPoint) throws Throwable {
        // 작업 시작 시간을 구합니다.
        long start = System.currentTimeMillis();

        // 위빙된 객체의 작업을 진행합니다.
        final T result = (T) joinPoint.proceed();

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        // ExecuteLog 어노테이션에 type 값에 들어간 타입을 추론합니다.
        Class<T> clazzType = this.classType(signature.getMethod().getAnnotations());

        String methodName = signature.getName();
        String input = Arrays.toString(signature.getParameterNames());

        String output = this.toString(result);

        log.info("Method Name : {}, Input : {}, Output : {}, Execute Time : {}", methodName, input, output, (System.currentTimeMillis() - start) + " ms");

        return result;
    }

    private <T> String toString(T result) throws Throwable {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for(Field field : result.getClass().getDeclaredFields()) {
            if(Strings.isBlank(field.getName())) {
                continue;
            }

            field.setAccessible(true);
            sb.append(field.getName()).append("=").append(field.get(result)).append(", ");
        }
        sb.append("]");

        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> classType(Annotation[] annotations) throws Throwable {
        Annotation executeLogAnnotation = Arrays.stream(annotations)
                .filter(a -> a.annotationType().getCanonicalName().equals(ExecuteLog.class.getCanonicalName()))
                .findFirst().orElseThrow(() -> new RuntimeException("ExecuteLog Annotation is not existed..."));

        String typeMethodName = "type";
        Method typeMethod = Arrays.stream(executeLogAnnotation.annotationType().getDeclaredMethods())
                .filter(m -> m.getName().equals(typeMethodName))
                .findFirst().orElseThrow(() -> new RuntimeException("type() of ExecuteLog is not existed..."));

        return (Class<T>) typeMethod.invoke(executeLogAnnotation);
    }
}
```

# Spring_Study_week6
