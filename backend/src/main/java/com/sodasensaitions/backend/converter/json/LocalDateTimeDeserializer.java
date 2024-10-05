package com.sodasensaitions.backend.converter.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;

public class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
  @Override
  public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return LocalDateTime.parse(json.getAsString(), Constants.DATE_TIME_FORMATTER);
  }
}
