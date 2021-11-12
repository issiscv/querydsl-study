package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public Optional<Member> findById(Long id) {
        Member member = em.find(Member.class, id);
        return Optional.of(member);
    }

    public List<Member> findAll() {
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    public List<Member> findAll_QueryDsl() {
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername_QueryDsl(String username) {
        return jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", username)
                .getResultList();
    }

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition memberSearchCond) {

        BooleanBuilder booleanBuilder = new BooleanBuilder();

        if (hasText(memberSearchCond.getUsername())) {
            booleanBuilder.and(member.username.eq(memberSearchCond.getUsername()));
        }

        if (hasText(memberSearchCond.getTeamName())) {
            booleanBuilder.and(team.name.eq(memberSearchCond.getTeamName()));
        }

        if (memberSearchCond.getAgeGoe() != null) {
            booleanBuilder.and(member.age.goe(memberSearchCond.getAgeGoe()));
        }

        if (memberSearchCond.getAgeLoe() != null) {
            booleanBuilder.and(member.age.loe(memberSearchCond.getAgeLoe()));
        }
        return jpaQueryFactory
                .select(new QMemberTeamDto(member.id, member.username, member.age, team.id, team.name))
                .from(member)
                .join(member.team, team)
                .where(booleanBuilder)
                .fetch();
    }

    public List<MemberTeamDto> search(MemberSearchCondition condition) {

        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id, member.username, member.age, team.id, team.name
                ))
                .from(member)
                .join(member.team, team)
                .where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()), ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
                .fetch();
    }

    public List<Member> searchAndMember(MemberSearchCondition condition) {

        //select projection 이 달라도 재사용이 가능하다.
        //조합 가능

        return jpaQueryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(ageBetween(condition.getAgeLoe(), condition.getAgeGoe()))
                .fetch();
    }

    private BooleanExpression ageBetween(int ageGoe, int ageLoe) {
        return member.age.between(ageLoe, ageGoe);
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe !=null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }
}
