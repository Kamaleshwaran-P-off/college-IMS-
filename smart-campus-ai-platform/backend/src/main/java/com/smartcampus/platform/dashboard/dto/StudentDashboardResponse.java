package com.smartcampus.platform.dashboard.dto;

import java.util.List;

public class StudentDashboardResponse {
  private double cgpa;
  private AttendanceSummary attendance;
  private ChartData assignmentMarks;
  private InternalMarks internalMarks;
  private ChartData quizPerformance;
  private CalendarInfo calendar;
  private List<TimetableDay> timetable;

  public StudentDashboardResponse() {}

  public StudentDashboardResponse(
      double cgpa,
      AttendanceSummary attendance,
      ChartData assignmentMarks,
      InternalMarks internalMarks,
      ChartData quizPerformance,
      CalendarInfo calendar,
      List<TimetableDay> timetable
  ) {
    this.cgpa = cgpa;
    this.attendance = attendance;
    this.assignmentMarks = assignmentMarks;
    this.internalMarks = internalMarks;
    this.quizPerformance = quizPerformance;
    this.calendar = calendar;
    this.timetable = timetable;
  }

  public double getCgpa() {
    return cgpa;
  }

  public void setCgpa(double cgpa) {
    this.cgpa = cgpa;
  }

  public AttendanceSummary getAttendance() {
    return attendance;
  }

  public void setAttendance(AttendanceSummary attendance) {
    this.attendance = attendance;
  }

  public ChartData getAssignmentMarks() {
    return assignmentMarks;
  }

  public void setAssignmentMarks(ChartData assignmentMarks) {
    this.assignmentMarks = assignmentMarks;
  }

  public InternalMarks getInternalMarks() {
    return internalMarks;
  }

  public void setInternalMarks(InternalMarks internalMarks) {
    this.internalMarks = internalMarks;
  }

  public ChartData getQuizPerformance() {
    return quizPerformance;
  }

  public void setQuizPerformance(ChartData quizPerformance) {
    this.quizPerformance = quizPerformance;
  }

  public CalendarInfo getCalendar() {
    return calendar;
  }

  public void setCalendar(CalendarInfo calendar) {
    this.calendar = calendar;
  }

  public List<TimetableDay> getTimetable() {
    return timetable;
  }

  public void setTimetable(List<TimetableDay> timetable) {
    this.timetable = timetable;
  }

  public static class AttendanceSummary {
    private double overallPercent;
    private List<ChartPoint> weekly;

    public AttendanceSummary() {}

    public AttendanceSummary(double overallPercent, List<ChartPoint> weekly) {
      this.overallPercent = overallPercent;
      this.weekly = weekly;
    }

    public double getOverallPercent() {
      return overallPercent;
    }

    public void setOverallPercent(double overallPercent) {
      this.overallPercent = overallPercent;
    }

    public List<ChartPoint> getWeekly() {
      return weekly;
    }

    public void setWeekly(List<ChartPoint> weekly) {
      this.weekly = weekly;
    }
  }

  public static class ChartPoint {
    private String label;
    private double percent;

    public ChartPoint() {}

    public ChartPoint(String label, double percent) {
      this.label = label;
      this.percent = percent;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public double getPercent() {
      return percent;
    }

    public void setPercent(double percent) {
      this.percent = percent;
    }
  }

  public static class ChartData {
    private List<String> labels;
    private List<Double> scores;

    public ChartData() {}

    public ChartData(List<String> labels, List<Double> scores) {
      this.labels = labels;
      this.scores = scores;
    }

    public List<String> getLabels() {
      return labels;
    }

    public void setLabels(List<String> labels) {
      this.labels = labels;
    }

    public List<Double> getScores() {
      return scores;
    }

    public void setScores(List<Double> scores) {
      this.scores = scores;
    }
  }

  public static class InternalMarks {
    private List<String> labels;
    private List<Double> cat1;
    private List<Double> cat2;
    private List<Double> cat3;

    public InternalMarks() {}

    public InternalMarks(List<String> labels, List<Double> cat1, List<Double> cat2, List<Double> cat3) {
      this.labels = labels;
      this.cat1 = cat1;
      this.cat2 = cat2;
      this.cat3 = cat3;
    }

    public List<String> getLabels() {
      return labels;
    }

    public void setLabels(List<String> labels) {
      this.labels = labels;
    }

    public List<Double> getCat1() {
      return cat1;
    }

    public void setCat1(List<Double> cat1) {
      this.cat1 = cat1;
    }

    public List<Double> getCat2() {
      return cat2;
    }

    public void setCat2(List<Double> cat2) {
      this.cat2 = cat2;
    }

    public List<Double> getCat3() {
      return cat3;
    }

    public void setCat3(List<Double> cat3) {
      this.cat3 = cat3;
    }
  }

  public static class CalendarInfo {
    private String url;
    private String contentType;
    private String fileName;

    public CalendarInfo() {}

    public CalendarInfo(String url, String contentType, String fileName) {
      this.url = url;
      this.contentType = contentType;
      this.fileName = fileName;
    }

    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getContentType() {
      return contentType;
    }

    public void setContentType(String contentType) {
      this.contentType = contentType;
    }

    public String getFileName() {
      return fileName;
    }

    public void setFileName(String fileName) {
      this.fileName = fileName;
    }
  }

  public static class TimetableDay {
    private String day;
    private List<TimetableSlot> slots;

    public TimetableDay() {}

    public TimetableDay(String day, List<TimetableSlot> slots) {
      this.day = day;
      this.slots = slots;
    }

    public String getDay() {
      return day;
    }

    public void setDay(String day) {
      this.day = day;
    }

    public List<TimetableSlot> getSlots() {
      return slots;
    }

    public void setSlots(List<TimetableSlot> slots) {
      this.slots = slots;
    }
  }

  public static class TimetableSlot {
    private String time;
    private String subject;
    private String room;

    public TimetableSlot() {}

    public TimetableSlot(String time, String subject, String room) {
      this.time = time;
      this.subject = subject;
      this.room = room;
    }

    public String getTime() {
      return time;
    }

    public void setTime(String time) {
      this.time = time;
    }

    public String getSubject() {
      return subject;
    }

    public void setSubject(String subject) {
      this.subject = subject;
    }

    public String getRoom() {
      return room;
    }

    public void setRoom(String room) {
      this.room = room;
    }
  }
}
