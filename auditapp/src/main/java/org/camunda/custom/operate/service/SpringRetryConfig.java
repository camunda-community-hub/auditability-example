package org.camunda.custom.operate.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@EnableRetry
public class SpringRetryConfig {

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    FixedBackOffPolicy fixedBackOffPolicy = new FixedBackOffPolicy();
    fixedBackOffPolicy.setBackOffPeriod(1000l);
    retryTemplate.setBackOffPolicy(fixedBackOffPolicy);

    SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
    retryPolicy.setMaxAttempts(10);
    retryTemplate.setRetryPolicy(retryPolicy);

    return retryTemplate;
  }
}
