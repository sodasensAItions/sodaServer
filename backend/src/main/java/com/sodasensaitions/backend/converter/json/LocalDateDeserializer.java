package com.sodasensaitions.backend.converter.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDate;

public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

  //https://www.javaguides.net/2018/10/gson-custom-serialization-and-deseriliazation-examples.html

  @Override
  public LocalDate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    return LocalDate.parse(json.getAsString(), Constants.DATE_FORMATTER);
  }
}
