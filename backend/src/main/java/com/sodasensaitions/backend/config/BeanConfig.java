package com.sodasensaitions.backend.config;

import com.sodasensaitions.backend.converter.json.LocalDateDeserializer;
import com.sodasensaitions.backend.converter.json.LocalDateSerializer;
import com.sodasensaitions.backend.converter.json.LocalDateTimeDeserializer;
import com.sodasensaitions.backend.converter.json.LocalDateTimeSerializer;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class BeanConfig {

  @Bean
  public Gson gson() {
    GsonBuilder gsonBuilder = new GsonBuilder();

    gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateSerializer());
    gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
    gsonBuilder.registerTypeAdapter(LocalDate.class, new LocalDateDeserializer());
    gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());

    return gsonBuilder.setPrettyPrinting().create();
  }

}
