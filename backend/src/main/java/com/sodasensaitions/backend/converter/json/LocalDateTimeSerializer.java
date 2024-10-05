package com.sodasensaitions.backend.converter.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {

  @Override
  public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
    return new JsonPrimitive(Constants.DATE_TIME_FORMATTER.format(localDateTime));
  }
}
