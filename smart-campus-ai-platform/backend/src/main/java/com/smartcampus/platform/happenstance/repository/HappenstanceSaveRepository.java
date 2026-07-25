package com.smartcampus.platform.happenstance.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartcampus.platform.happenstance.entity.HappenstanceSave;

public interface HappenstanceSaveRepository extends JpaRepository<HappenstanceSave, Long> {
  List<HappenstanceSave> findByUserId(Long userId);

  Optional<HappenstanceSave> findByUserIdAndOpportunityId(Long userId, Long opportunityId);

  long countByOpportunityId(Long opportunityId);

  @Query("select o.domain, count(s) from HappenstanceSave s join s.opportunity o group by o.domain order by count(s) desc")
  List<Object[]> countSavesByDomain();

  @Query("select o.id, o.title, count(s) from HappenstanceSave s join s.opportunity o group by o.id, o.title order by count(s) desc")
  List<Object[]> countSavesByOpportunity();

  @Query("select distinct s.userId from HappenstanceSave s")
  List<Long> findDistinctUserIds();
}
