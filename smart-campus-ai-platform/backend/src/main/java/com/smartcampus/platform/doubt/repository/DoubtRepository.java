package com.smartcampus.platform.doubt.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcampus.platform.doubt.dto.LeaderboardEntry;
import com.smartcampus.platform.doubt.entity.Doubt;
import com.smartcampus.platform.doubt.entity.DoubtStatus;

public interface DoubtRepository extends JpaRepository<Doubt, Long> {
  @Query("""
      select d from Doubt d
      where (:status is null or d.status = :status)
        and (:assignmentId is null or d.assignment.id = :assignmentId)
        and (:studentUserId is null or d.student.user.id = :studentUserId)
        and (:accepted is null or (:accepted = true and d.acceptedAnswer is not null) or (:accepted = false and d.acceptedAnswer is null))
        and (:search is null
          or lower(d.title) like lower(concat('%', :search, '%'))
          or d.description like concat('%', :search, '%'))
      """)
  Page<Doubt> search(
      @Param("status") DoubtStatus status,
      @Param("assignmentId") Long assignmentId,
      @Param("studentUserId") Long studentUserId,
      @Param("accepted") Boolean accepted,
      @Param("search") String search,
      Pageable pageable
  );

  @Query("""
      select new com.smartcampus.platform.doubt.dto.LeaderboardEntry(a.author.id, a.author.fullName, count(d))
      from Doubt d
      join d.acceptedAnswer a
      where a.author.role = com.smartcampus.platform.auth.entity.Role.STUDENT
      group by a.author.id, a.author.fullName
      order by count(d) desc
      """)
  List<LeaderboardEntry> findLeaderboard(Pageable pageable);
}
