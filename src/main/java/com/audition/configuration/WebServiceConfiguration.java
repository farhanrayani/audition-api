package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebServiceConfiguration implements WebMvcConfigurer {

    private static final String YEAR_MONTH_DAY_PATTERN = "yyyy-MM-dd";
    private static final Logger LOG = LoggerFactory.getLogger(WebServiceConfiguration.class);

    @Autowired
    private AuditionLogger auditionLogger;

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // 1. Allow for date format as yyyy-MM-dd
        mapper.setDateFormat(new SimpleDateFormat(YEAR_MONTH_DAY_PATTERN));
        mapper.registerModule(new JavaTimeModule());

        // 2. Does not fail on unknown properties
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 3. Maps to camelCase
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);

        // 4. Does not include null values or empty values
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);

        // 5. Does not write dates as timestamps
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

    @Bean
    public RestTemplate restTemplate() {
        final RestTemplate restTemplate = new RestTemplate(
                new BufferingClientHttpRequestFactory(createClientFactory()));

        // Use object mapper
        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        messageConverters.removeIf(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        messageConverters.add(new MappingJackson2HttpMessageConverter(objectMapper()));

        // Create a logging interceptor that logs request/response for rest template calls
        restTemplate.setInterceptors(List.of(new RestTemplateLoggingInterceptor()));

        return restTemplate;
    }

    private SimpleClientHttpRequestFactory createClientFactory() {
        final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setOutputStreaming(false);
        return requestFactory;
    }

    private class RestTemplateLoggingInterceptor implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {

            // Log request
            auditionLogger.info(LOG, "RestTemplate Request: {} {}", request.getMethod(), request.getURI());
            if (body.length > 0) {
                auditionLogger.debug(LOG, "RestTemplate Request Body: {}", new String(body, StandardCharsets.UTF_8));
            }

            // Execute request
            ClientHttpResponse response = execution.execute(request, body);

            // Log response
            auditionLogger.info(LOG, "RestTemplate Response: {} for {} {}",
                    response.getStatusCode(), request.getMethod(), request.getURI());

            return response;
        }
    }
}