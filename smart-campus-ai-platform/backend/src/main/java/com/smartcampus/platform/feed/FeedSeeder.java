package com.smartcampus.platform.feed;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartcampus.platform.feed.entity.CareerCategory;
import com.smartcampus.platform.feed.entity.CareerFeedItem;
import com.smartcampus.platform.feed.entity.MicroFeedItem;
import com.smartcampus.platform.feed.entity.MicroFeedType;
import com.smartcampus.platform.feed.repository.CareerFeedItemRepository;
import com.smartcampus.platform.feed.repository.MicroFeedItemRepository;

@Component
public class FeedSeeder implements CommandLineRunner {
  private final CareerFeedItemRepository careerFeedItemRepository;
  private final MicroFeedItemRepository microFeedItemRepository;

  public FeedSeeder(
      CareerFeedItemRepository careerFeedItemRepository,
      MicroFeedItemRepository microFeedItemRepository
  ) {
    this.careerFeedItemRepository = careerFeedItemRepository;
    this.microFeedItemRepository = microFeedItemRepository;
  }

  @Override
  public void run(String... args) {
    if (careerFeedItemRepository.count() == 0) {
      careerFeedItemRepository.save(new CareerFeedItem(
          "Frontend Roadmap 2026",
          "Campus Mentors",
          "A crisp path to master modern frontend stacks, design systems, and performance workflows.",
          CareerCategory.WEB_DEVELOPMENT,
          "https://www.linkedin.com",
          null,
          LocalDateTime.now().minusDays(2)
      ));
      careerFeedItemRepository.save(new CareerFeedItem(
          "AI/ML Internship Planner",
          "AI Guild",
          "Find labs, open-source programs, and portfolio checkpoints for a top-tier AI internship.",
          CareerCategory.AI_ML,
          "https://www.kaggle.com",
          null,
          LocalDateTime.now().minusDays(1)
      ));
      careerFeedItemRepository.save(new CareerFeedItem(
          "Placement Sprint Checklist",
          "Career Cell",
          "Weekly preparation plan with DSA, system design, and mock interviews.",
          CareerCategory.PLACEMENTS,
          "https://www.instagram.com",
          null,
          LocalDateTime.now().minusHours(8)
      ));
      careerFeedItemRepository.save(new CareerFeedItem(
          "Summer Internship Tracker",
          "Alumni Hub",
          "A curated list of internships plus application tips and quick resume enhancements.",
          CareerCategory.INTERNSHIPS,
          "https://www.internshala.com",
          null,
          LocalDateTime.now().minusHours(4)
      ));
    }

    if (microFeedItemRepository.count() == 0) {
      microFeedItemRepository.save(new MicroFeedItem(
          "React State in 60s",
          "useState basics: state value, setter, and why it triggers re-render.",
          MicroFeedType.TEXT,
          null,
          LocalDateTime.now().minusHours(10)
      ));
      microFeedItemRepository.save(new MicroFeedItem(
          "Career Tip: Portfolio in 3 steps",
          "Pick a theme, build a case study, and ship a demo. Keep it crisp.",
          MicroFeedType.VIDEO,
          "https://www.w3schools.com/html/mov_bbb.mp4",
          LocalDateTime.now().minusHours(9)
      ));
      microFeedItemRepository.save(new MicroFeedItem(
          "Git Branching Flow",
          "Feature branches -> PR -> review -> merge. Keep commits small and meaningful.",
          MicroFeedType.TEXT,
          null,
          LocalDateTime.now().minusHours(6)
      ));
      microFeedItemRepository.save(new MicroFeedItem(
          "Quick SQL Joins",
          "INNER joins match rows in both tables, LEFT joins keep all rows in left table.",
          MicroFeedType.TEXT,
          null,
          LocalDateTime.now().minusHours(3)
      ));
    }
  }
}
