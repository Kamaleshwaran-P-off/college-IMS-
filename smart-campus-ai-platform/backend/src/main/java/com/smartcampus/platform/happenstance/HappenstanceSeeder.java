package com.smartcampus.platform.happenstance;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.smartcampus.platform.happenstance.entity.HappenstanceOpportunity;
import com.smartcampus.platform.happenstance.repository.HappenstanceOpportunityRepository;

@Component
public class HappenstanceSeeder implements CommandLineRunner {
  private final HappenstanceOpportunityRepository repository;

  public HappenstanceSeeder(HappenstanceOpportunityRepository repository) {
    this.repository = repository;
  }

  @Override
  public void run(String... args) {
    if (repository.count() > 0) {
      return;
    }

    repository.save(new HappenstanceOpportunity(
        "AI Campus Hack Sprint",
        "Build AI-powered solutions for student life with mentors and quick prizes.",
        "AI",
        "https://unstop.com/hackathons",
        "Unstop",
        "Hackathon",
        "AI,Data Science",
        "Apr 22, 2026",
        "Online",
        true,
        LocalDateTime.now().minusDays(3)
    ));

    repository.save(new HappenstanceOpportunity(
        "Smart City Design Jam",
        "Design-first sprint on urban mobility, sustainability, and citizen UX.",
        "Design",
        "https://www.knowafest.com/explore/events?city=Chennai",
        "Knowafest",
        "Event",
        "Design,Product",
        "May 06, 2026",
        "Chennai",
        false,
        LocalDateTime.now().minusDays(2)
    ));

    repository.save(new HappenstanceOpportunity(
        "Startup GTM Fellowship",
        "Work with founders on growth experiments and go-to-market strategy.",
        "Startup",
        "https://unstop.com/internships",
        "Unstop",
        "Internship",
        "Startup,Product",
        "Rolling",
        "Remote",
        false,
        LocalDateTime.now().minusDays(1)
    ));

    repository.save(new HappenstanceOpportunity(
        "Full Stack Build Weekend",
        "Ship a full-stack MVP in 48 hours with live demos and judges.",
        "Web Dev",
        "https://unstop.com/hackathons",
        "Unstop",
        "Hackathon",
        "Web Dev,Open Source",
        "May 19, 2026",
        "Online",
        true,
        LocalDateTime.now().minusHours(20)
    ));

    repository.save(new HappenstanceOpportunity(
        "Data Science Micro-Internship",
        "Analyze campus data, build dashboards, and present findings to faculty.",
        "Data Science",
        "https://unstop.com/internships",
        "Unstop",
        "Internship",
        "Data Science,AI",
        "Apr 30, 2026",
        "Remote",
        false,
        LocalDateTime.now().minusHours(18)
    ));

    repository.save(new HappenstanceOpportunity(
        "Cybersecurity Capture the Flag",
        "Hands-on CTF for incident response and red-team fundamentals.",
        "Cybersecurity",
        "https://www.knowafest.com/explore/events?city=Chennai",
        "Knowafest",
        "Event",
        "Cybersecurity,AI",
        "May 25, 2026",
        "Chennai",
        false,
        LocalDateTime.now().minusHours(16)
    ));

    repository.save(new HappenstanceOpportunity(
        "IoT for Agriculture Challenge",
        "Build sensor-driven prototypes for precision agriculture.",
        "IoT",
        "https://unstop.com/hackathons",
        "Unstop",
        "Hackathon",
        "IoT,Product",
        "Jun 02, 2026",
        "Online",
        false,
        LocalDateTime.now().minusHours(12)
    ));

    repository.save(new HappenstanceOpportunity(
        "Mobile UX Sprint",
        "Craft mobile UX flows for campus apps with quick feedback loops.",
        "Mobile",
        "https://www.knowafest.com/explore/events?city=Chennai",
        "Knowafest",
        "Event",
        "Design,Mobile",
        "Jun 12, 2026",
        "Chennai",
        false,
        LocalDateTime.now().minusHours(10)
    ));

    repository.save(new HappenstanceOpportunity(
        "Open Source Contributor Drive",
        "Join open source maintainers, fix issues, and earn badges.",
        "Open Source",
        "https://unstop.com/competitions",
        "Unstop",
        "Event",
        "Open Source,Web Dev",
        "May 30, 2026",
        "Online",
        false,
        LocalDateTime.now().minusHours(8)
    ));

    repository.save(new HappenstanceOpportunity(
        "AI + Design Ideathon",
        "Blend AI tools with design thinking to solve campus challenges.",
        "AI",
        "https://www.knowafest.com/explore/events?city=Chennai",
        "Knowafest",
        "Hackathon",
        "AI,Design",
        "Apr 27, 2026",
        "Chennai",
        true,
        LocalDateTime.now().minusHours(4)
    ));
  }
}
