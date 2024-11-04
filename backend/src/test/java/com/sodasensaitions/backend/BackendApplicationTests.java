package com.sodasensaitions.backend;

import org.junit.jupiter.api.Test;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SelectPackages({"com.sodasensaitions.backend.utils",
    "com.sodasensaitions.backend.accounttests",
    "com.sodasensaitions.backend.ingredienttests",
})
@Suite
class BackendApplicationTests {

  @Test
  void contextLoads() {
  }

}
