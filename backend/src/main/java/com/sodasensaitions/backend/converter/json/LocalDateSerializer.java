package com.sodasensaitions.backend.converter.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class LocalDateSerializer implements JsonSerializer<LocalDate> {

  @Override
  public JsonElement serialize(LocalDate localDate, Type srcType, JsonSerializationContext context) {
    return new JsonPrimitive(Constants.DATE_FORMATTER.format(localDate));
  }
}
