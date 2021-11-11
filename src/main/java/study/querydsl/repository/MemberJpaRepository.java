package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QTeam;

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
}
