package com.sodasensaitions.backend.authentication.testcontrollers;

import com.sodasensaitions.backend.config.constants.HttpServletSessionConstants;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {

  private final HttpServletRequest request;

  @GetMapping(path = "/public")
  public ResponseEntity<String> sayHello() {
    return ResponseEntity.ok("Hello from public endpoint");
  }

  @GetMapping(path = "/unreachable")
  public ResponseEntity<String> sayHelloSecured() {
    String principal = (String) request.getSession().getAttribute(HttpServletSessionConstants.PRINCIPAL);
    return ResponseEntity.ok(principal);
  }
}
