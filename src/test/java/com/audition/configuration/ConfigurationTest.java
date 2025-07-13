package com.audition.configuration;

import com.audition.common.logging.AuditionLogger;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfigurationTest {

    @Mock
    private AuditionLogger auditionLogger;

    @InjectMocks
    private WebServiceConfiguration webServiceConfiguration;

    @Test
    void testCacheConfiguration() {
        // Given
        CacheConfiguration cacheConfiguration = new CacheConfiguration();

        // When
        CacheManager cacheManager = cacheConfiguration.cacheManager();

        // Then
        assertNotNull(cacheManager);
        assertTrue(cacheManager instanceof CaffeineCacheManager);

        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;
        assertNotNull(caffeineCacheManager.getCacheNames());
        assertTrue(caffeineCacheManager.getCacheNames().contains("posts"));
        assertTrue(caffeineCacheManager.getCacheNames().contains("posts-with-comments"));
        assertTrue(caffeineCacheManager.getCacheNames().contains("comments"));
    }

    @Test
    void testObjectMapperConfiguration() {
        // When
        ObjectMapper objectMapper = webServiceConfiguration.objectMapper();

        // Then
        assertNotNull(objectMapper);

        // Test date format
        assertTrue(objectMapper.getDateFormat() instanceof SimpleDateFormat);

        // Test JavaTimeModule is registered
        assertTrue(objectMapper.getRegisteredModuleIds().contains("jackson-datatype-jsr310"));

        // Test configuration settings
        assertFalse(objectMapper.getDeserializationConfig().isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
        assertEquals(PropertyNamingStrategies.LOWER_CAMEL_CASE, objectMapper.getPropertyNamingStrategy());
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_NULL_MAP_VALUES));
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS));
        assertFalse(objectMapper.getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    void testRestTemplateConfiguration() {
        // When
        RestTemplate restTemplate = webServiceConfiguration.restTemplate();

        // Then
        assertNotNull(restTemplate);

        // Check that message converters include our custom ObjectMapper
        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
        boolean hasMappingJacksonConverter = converters.stream()
                .anyMatch(converter -> converter instanceof MappingJackson2HttpMessageConverter);
        assertTrue(hasMappingJacksonConverter);

        // Check that interceptors are set
        assertNotNull(restTemplate.getInterceptors());
        assertEquals(1, restTemplate.getInterceptors().size());
    }

    @Test
    void testResponseHeaderInjectorDoFilter() throws Exception {
        // Given
        ResponseHeaderInjector injector = new ResponseHeaderInjector();
        ServletRequest request = mock(ServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        // When
        injector.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        // Headers might be set if MDC contains trace/span IDs, but we don't set them in this test
    }

    @Test
    void testResponseHeaderInjectorInitAndDestroy() throws Exception {
        // Given
        ResponseHeaderInjector injector = new ResponseHeaderInjector();

        // When & Then - these methods should not throw exceptions
        injector.init(null);
        injector.destroy();
    }

    @Test
    void testResponseHeaderInjectorWithNonHttpResponse() throws Exception {
        // Given
        ResponseHeaderInjector injector = new ResponseHeaderInjector();
        ServletRequest request = mock(ServletRequest.class);
        ServletResponse response = mock(ServletResponse.class); // Not HttpServletResponse
        FilterChain chain = mock(FilterChain.class);

        // When
        injector.doFilter(request, response, chain);

        // Then
        verify(chain).doFilter(request, response);
        // No headers should be set since it's not an HttpServletResponse
    }

    @Test
    void testCacheConfigurationAnnotations() {
        // Given
        CacheConfiguration cacheConfiguration = new CacheConfiguration();

        // When
        Class<?> clazz = cacheConfiguration.getClass();

        // Then
        assertTrue(clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(clazz.isAnnotationPresent(org.springframework.cache.annotation.EnableCaching.class));
        assertTrue(clazz.isAnnotationPresent(org.springframework.scheduling.annotation.EnableScheduling.class));
    }

    @Test
    void testWebServiceConfigurationAnnotations() {
        // Given
        WebServiceConfiguration webServiceConfiguration = new WebServiceConfiguration();

        // When
        Class<?> clazz = webServiceConfiguration.getClass();

        // Then
        assertTrue(clazz.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
    }

    @Test
    void testResponseHeaderInjectorAnnotations() {
        // Given
        ResponseHeaderInjector injector = new ResponseHeaderInjector();

        // When
        Class<?> clazz = injector.getClass();

        // Then
        assertTrue(clazz.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testCaffeineCacheConfiguration() {
        // Given
        CacheConfiguration cacheConfiguration = new CacheConfiguration();

        // When
        CacheManager cacheManager = cacheConfiguration.cacheManager();
        CaffeineCacheManager caffeineCacheManager = (CaffeineCacheManager) cacheManager;



        // Create a cache to test the configuration
        org.springframework.cache.Cache postsCache = cacheManager.getCache("posts");
        assertNotNull(postsCache);

        // Test cache operations
        postsCache.put("test-key", "test-value");
        assertEquals("test-value", postsCache.get("test-key", String.class));
    }
}