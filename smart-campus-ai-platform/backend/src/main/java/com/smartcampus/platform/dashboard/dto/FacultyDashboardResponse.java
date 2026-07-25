package com.smartcampus.platform.dashboard.dto;

import java.util.List;

public class FacultyDashboardResponse {
  private AttendanceSeries attendanceByDay;
  private InternalMarks averageInternalMarks;
  private ChartData averageAssignmentMarks;
  private List<TodayClass> todayClasses;

  public FacultyDashboardResponse() {}

  public FacultyDashboardResponse(
      AttendanceSeries attendanceByDay,
      InternalMarks averageInternalMarks,
      ChartData averageAssignmentMarks,
      List<TodayClass> todayClasses
  ) {
    this.attendanceByDay = attendanceByDay;
    this.averageInternalMarks = averageInternalMarks;
    this.averageAssignmentMarks = averageAssignmentMarks;
    this.todayClasses = todayClasses;
  }

  public AttendanceSeries getAttendanceByDay() {
    return attendanceByDay;
  }

  public void setAttendanceByDay(AttendanceSeries attendanceByDay) {
    this.attendanceByDay = attendanceByDay;
  }

  public InternalMarks getAverageInternalMarks() {
    return averageInternalMarks;
  }

  public void setAverageInternalMarks(InternalMarks averageInternalMarks) {
    this.averageInternalMarks = averageInternalMarks;
  }

  public ChartData getAverageAssignmentMarks() {
    return averageAssignmentMarks;
  }

  public void setAverageAssignmentMarks(ChartData averageAssignmentMarks) {
    this.averageAssignmentMarks = averageAssignmentMarks;
  }

  public List<TodayClass> getTodayClasses() {
    return todayClasses;
  }

  public void setTodayClasses(List<TodayClass> todayClasses) {
    this.todayClasses = todayClasses;
  }

  public static class AttendanceSeries {
    private List<String> labels;
    private List<Series> series;

    public AttendanceSeries() {}

    public AttendanceSeries(List<String> labels, List<Series> series) {
      this.labels = labels;
      this.series = series;
    }

    public List<String> getLabels() {
      return labels;
    }

    public void setLabels(List<String> labels) {
      this.labels = labels;
    }

    public List<Series> getSeries() {
      return series;
    }

    public void setSeries(List<Series> series) {
      this.series = series;
    }
  }

  public static class Series {
    private String label;
    private List<Double> values;

    public Series() {}

    public Series(String label, List<Double> values) {
      this.label = label;
      this.values = values;
    }

    public String getLabel() {
      return label;
    }

    public void setLabel(String label) {
      this.label = label;
    }

    public List<Double> getValues() {
      return values;
    }

    public void setValues(List<Double> values) {
      this.values = values;
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

  public static class TodayClass {
    private String time;
    private String className;
    private String subject;
    private String room;

    public TodayClass() {}

    public TodayClass(String time, String className, String subject, String room) {
      this.time = time;
      this.className = className;
      this.subject = subject;
      this.room = room;
    }

    public String getTime() {
      return time;
    }

    public void setTime(String time) {
      this.time = time;
    }

    public String getClassName() {
      return className;
    }

    public void setClassName(String className) {
      this.className = className;
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
