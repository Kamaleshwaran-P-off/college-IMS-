package com.smartcampus.platform.happenstance.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartcampus.platform.happenstance.entity.HappenstanceClick;

public interface HappenstanceClickRepository extends JpaRepository<HappenstanceClick, Long> {
  List<HappenstanceClick> findByUserId(Long userId);

  @Query("select o.domain, count(c) from HappenstanceClick c join c.opportunity o group by o.domain order by count(c) desc")
  List<Object[]> countClicksByDomain();

  @Query("select distinct c.userId from HappenstanceClick c")
  List<Long> findDistinctUserIds();
}
