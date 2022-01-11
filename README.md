# 인프런 김영한님의 '실전! Querydsl' 을 학습한 프로젝트

## 기본 Q-Type
### Q 클래스 인스턴스를 사용하는 2가지 방법

    QMember qMember = new QMember("m"); //별칭 직접 지정
    QMember qMember = QMember.member; //기본 인스턴스 사용

> Qmember.member 를 static import 하여 사용 권장

## 검색 조건 쿼리
## JPQL 이 제공하는 모든 검색 조건


    member.username.eq("member1") // username = 'member1'
    member.username.ne("member1") //username != 'member1'
    member.username.eq("member1").not() // username != 'member1'
    member.username.isNotNull() //이름이 is not null
    member.age.in(10, 20) // age in (10,20)
    member.age.notIn(10, 20) // age not in (10, 20)
    member.age.between(10,30) //between 10, 30
    member.age.goe(30) // age >= 30
    member.age.gt(30) // age > 30
    member.age.loe(30) // age <= 30
    member.age.lt(30) // age < 30
    member.username.like("member%") //like 검색
    member.username.contains("member") // like ‘%member%’ 검색
    member.username.startsWith("member") //like ‘member%’ 검색
    ...

- where() 에 파라미터로 검색조건을 추가하면 AND 조건이 추가됨
  이 경우 null 값은 무시
> 검색 조건은 .and()와 .or()를 메서드 체인으로 연결할 수 있다.

## 결과 조회
- fetch() : 리스트 조회, 데이터 없으면 빈 리스트 반환
- fetchOne() : 단 건 조회
    1. 결과가 없으면 : null
    2. 결과가 둘 이상이면 : com.querydsl.core.NonUniqueResultException
- fetchFirst() : limit(1).fetchOne()
- fetchResults() : 페이징 정보 포함, total count 쿼리 추가 실행
- fetchCount() : count 쿼리로 변경해서 count 수 조회

        
    //List
    List<Member> fetch = queryFactory
    .selectFrom(member)
    .fetch();

    //단 건
    Member findMember1 = queryFactory
    .selectFrom(member)
    .fetchOne();

    //처음 한 건 조회
    Member findMember2 = queryFactory
    .selectFrom(member)
    .fetchFirst();

    //페이징에서 사용
    QueryResults<Member> results = queryFactory
    .selectFrom(member)
    .fetchResults();

    //count 쿼리로 변경
    long count = queryFactory
    .selectFrom(member)
    .fetchCount();

## 정렬

    List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.age.eq(100))
    .orderBy(member.age.desc(), member.username.asc().nullsLast())
    .fetch();

- desc() , asc() : 일반 정렬
- nullsLast() , nullsFirst() : null 데이터 순서 부여

## 페이징

    QueryResults<Member> queryResults = queryFactory
    .selectFrom(member)
    .orderBy(member.username.desc())
    .offset(1)
    .limit(2)
    .fetchResults();
    
    queryResults.getTotal());//전체 데이터 수
    queryResults.getLimit());//몇개로 제한할 지
    queryResults.getOffset());//offset: 몇번 째 부터
    queryResults.getResults().size());//페이징 결과의 데이터 수


- fetchResults() 를 사용할 경우 count 쿼리가 먼저 나간다.

> 실무에서 페이징 쿼리를 작성할 때, 데이터를 조회하는 쿼리는 여러 테이블을 조인해야 하지만,
count 쿼리는 조인이 필요 없는 경우도 있다. 그런데 이렇게 자동화된 count 쿼리는 원본 쿼리와 같이 모두
조인을 해버리기 때문에 성능이 안나올 수 있다. count 쿼리에 조인이 필요없는 성능 최적화가 필요하다면,
count 전용 쿼리를 별도로 작성해야 한다

## 집합
### 집합 함수
    
    List<Tuple> result = queryFactory
    .select(member.count(),
    member.age.sum(),
    member.age.avg(),
    member.age.max(),
    member.age.min())
    .from(member)
    .fetch();

### groupBy(), having() 예시

     …
    .groupBy(item.price)
    .having(item.price.gt(1000))
    …    

## 조인 - 기본 조인
- 조인의 기본 문법은 첫 번째 파라미터에 조인 대상을 지정하고, 두 번째 파라미터에 별칭(alias)으로 사용할
  Q 타입을 지정하면 된다


    join(조인 대상, 별칭으로 사용할 Q타입)

### 기본 조인

     List<Member> result = queryFactory
    .selectFrom(member)
    .join(member.team, team)
    .where(team.name.eq("teamA"))
    .fetch();

- join() , innerJoin() : 내부 조인(inner join)
- leftJoin() : left 외부 조인(left outer join)
- rightJoin() : rigth 외부 조인(rigth outer join)

## 세타 조인
- 연관관계가 없는 필드로 조인


      List<Member> result = queryFactory
      .select(member)
      .from(member, team)
      .where(member.username.eq(team.name))
      .fetch();

- from 절에 여러 엔티티를 선택해서 세타 조인
- 외부 조인 불가능 다음에 설명할 조인 on을 사용하면 외부 조인 가능

## 조인 - 페치조인

    Member findMember = queryFactory
    .selectFrom(member)
    .join(member.team, team).fetchJoin()
    .where(member.username.eq("member1"))
    .fetchOne();

### 사용방법
- join(), leftJoin() 등 조인 기능 뒤에 fetchJoin() 이라고 추가하면 된다.

## 서브 쿼리
### 나이가 평균 나이 이상인 회원 조회

    List<Member> result = queryFactory
    .selectFrom(member)
    .where(member.age.goe(
    JPAExpressions
    .select(memberSub.age.avg())
    .from(memberSub)
    ))
    .fetch();

### select 절에 subquery

    List<Tuple> fetch = queryFactory
    .select(member.username,
    JPAExpressions
    .select(memberSub.age.avg())
    .from(memberSub)
    ).from(member)
    .fetch();

- 서브쿼리를 사용할때는 JpaExpressions 를 사용하자 

> from 절의 서브쿼리 한계<br>
JPA JPQL 서브쿼리의 한계점으로 from 절의 서브쿼리(인라인 뷰)는 지원하지 않는다. 당연히 Querydsl
도 지원하지 않는다. 하이버네이트 구현체를 사용하면 select 절의 서브쿼리는 지원한다. Querydsl도
하이버네이트 구현체를 사용하면 select 절의 서브쿼리를 지원한다.

### from 절의 서브쿼리 해결방안
1. 서브쿼리를 join으로 변경한다. (가능한 상황도 있고, 불가능한 상황도 있다.)
2. 애플리케이션에서 쿼리를 2번 분리해서 실행한다.
3. nativeSQL을 사용한다

## Case 문
### 단순한 조건

    List<String> result = queryFactory
    .select(member.age
    .when(10).then("열살")
    .when(20).then("스무살")
    .otherwise("기타"))
    .from(member)
    .fetch();

### 복잡한 조건

    List<String> result = queryFactory
    .select(new CaseBuilder()
    .when(member.age.between(0, 20)).then("0~20살")
    .when(member.age.between(21, 30)).then("21~30살")
    .otherwise("기타"))
    .from(member)
    .fetch();

- new CaseBuilder() 를 사용해 복잡한 Case 문을 작성한다.

## 상수, 문자 더하기
- 상수가 필요하면 Expressions.constant(xxx) 사용

### 상수

    Tuple result = queryFactory
    .select(member.username, Expressions.constant("A"))
    .from(member)
    .fetchFirst();

### 문자 더하기

    String result = queryFactory
    .select(member.username.concat("_").concat(member.age.stringValue()))
    .from(member)
    .where(member.username.eq("member1"))
    .fetchOne();

> member.age.stringValue() 부분이 중요한데, 문자가 아닌 다른 타입들은 stringValue() 로
문자로 변환할 수 있다. 이 방법은 ENUM을 처리할 때도 자주 사용한다.