package com.wipro.demp.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wipro.demp.constants.DempConstants;

@Configuration
@ConditionalOnClass(name = DempConstants.RAZORPAY_CLIENT)
public class RazorpayConfig {

  @Value("${razorpay.keyId}")
  private String keyId;

  @Value("${razorpay.keySecret}")
  private String keySecret;

  @Bean
  public Object razorpayClient() throws Exception {
    Class<?> clientClass = Class.forName(DempConstants.RAZORPAY_CLIENT);
    return clientClass.getConstructor(String.class, String.class).newInstance(keyId, keySecret);
  }
}
