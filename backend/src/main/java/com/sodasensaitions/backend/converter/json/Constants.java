package com.sodasensaitions.backend.converter.json;

import java.time.format.DateTimeFormatter;

public class Constants {
  //constant representing the basic format a datetime must have
  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
  public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private Constants() {
  }
}
