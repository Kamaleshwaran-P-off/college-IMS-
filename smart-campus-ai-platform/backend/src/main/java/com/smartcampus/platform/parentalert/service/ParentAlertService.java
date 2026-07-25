package com.smartcampus.platform.parentalert.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.attendance.entity.AttendanceStatus;
import com.smartcampus.platform.attendance.repository.AttendanceRepository;
import com.smartcampus.platform.parentalert.entity.ParentAlert;
import com.smartcampus.platform.parentalert.repository.ParentAlertRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class ParentAlertService {
  private static final Logger log = LoggerFactory.getLogger(ParentAlertService.class);

  private final StudentRepository studentRepository;
  private final AttendanceRepository attendanceRepository;
  private final ParentAlertRepository parentAlertRepository;
  private final WhatsAppClient whatsAppClient;
  private final double threshold;
  private final int daysWindow;

  public ParentAlertService(
      StudentRepository studentRepository,
      AttendanceRepository attendanceRepository,
      ParentAlertRepository parentAlertRepository,
      WhatsAppClient whatsAppClient,
      @Value("${app.parent-alerts.attendance-threshold:75}") double threshold,
      @Value("${app.parent-alerts.days-window:30}") int daysWindow
  ) {
    this.studentRepository = studentRepository;
    this.attendanceRepository = attendanceRepository;
    this.parentAlertRepository = parentAlertRepository;
    this.whatsAppClient = whatsAppClient;
    this.threshold = threshold;
    this.daysWindow = daysWindow;
  }

  @Scheduled(cron = "0 30 8 * * *")
  public void sendAlerts() {
    if (!whatsAppClient.isConfigured()) {
      return;
    }

    LocalDate today = LocalDate.now();
    LocalDate start = today.minusDays(Math.max(daysWindow - 1, 0));

    List<Student> students = studentRepository.findAll();
    for (Student student : students) {
      if (student.getParentPhone() == null || student.getParentPhone().isBlank()) {
        continue;
      }

      if (parentAlertRepository.existsByStudentIdAndAlertDate(student.getId(), today)) {
        continue;
      }

      long total = attendanceRepository.countByStudentIdAndClassDateBetween(student.getId(), start, today);
      if (total == 0) {
        continue;
      }
      long present = attendanceRepository.countByStudentIdAndClassDateBetweenAndStatus(
          student.getId(), start, today, AttendanceStatus.PRESENT);

      double percent = (present * 100.0) / total;
      if (percent >= threshold) {
        continue;
      }

      String message = buildMessage(student, percent);
      try {
        whatsAppClient.sendMessage(student.getParentPhone(), message);
        ParentAlert alert = new ParentAlert(student, today, percent, "WHATSAPP", message);
        parentAlertRepository.save(alert);
      } catch (Exception ex) {
        log.warn("Failed to send parent alert for student {}: {}", student.getId(), ex.getMessage());
      }
    }
  }

  private String buildMessage(Student student, double percent) {
    String name = student.getUser().getFullName();
    String register = student.getStudentCode();
    return "Smart Campus Alert: " + name + " (Reg: " + register + ") attendance is "
        + String.format("%.1f", percent) + "%. Please ensure attendance improves.";
  }
}
