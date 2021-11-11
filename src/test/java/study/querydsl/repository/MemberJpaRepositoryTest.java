package study.querydsl.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {

    @Autowired
    private MemberJpaRepository memberJpaRepository;
    @Autowired
    private EntityManager em;

    @Test
    public void basic() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
        assertThat(findMember).isEqualTo(member);

        List<Member> result = memberJpaRepository.findByUsername("member1");

        assertThat(result).containsExactly(member);
    }

    @Test
    public void basic_QueryDsl() {
        Member member = new Member("member1", 10);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).orElse(null);
        assertThat(findMember).isEqualTo(member);

        List<Member> all_queryDsl = memberJpaRepository.findAll_QueryDsl();
        assertThat(all_queryDsl).containsExactly(member);

        List<Member> result = memberJpaRepository.findByUsername_QueryDsl("member1");
        assertThat(result).containsExactly(member);
    }

    @Test
    void searchTest() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition memberSearchCondition = new MemberSearchCondition();
        memberSearchCondition.setAgeGoe(35);
        memberSearchCondition.setAgeLoe(45);

        List<MemberTeamDto> memberTeamDtos = memberJpaRepository
                .searchByBuilder(memberSearchCondition);

        assertThat(memberTeamDtos).extracting("username").containsExactly("member4");

    }
}