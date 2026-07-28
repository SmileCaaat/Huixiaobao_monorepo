package com.ruoyi.framework.config;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Ensure JSON responses declare UTF-8 charset (avoid mojibake on login toasts).
 * i18n encoding is configured via spring.messages.encoding in application.yml.
 */
@Configuration
public class Utf8WebConfig implements WebMvcConfigurer
{
    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        for (HttpMessageConverter<?> converter : converters)
        {
            if (converter instanceof MappingJackson2HttpMessageConverter)
            {
                MappingJackson2HttpMessageConverter jsonConverter = (MappingJackson2HttpMessageConverter) converter;
                jsonConverter.setDefaultCharset(StandardCharsets.UTF_8);
                jsonConverter.setSupportedMediaTypes(Arrays.asList(
                        new MediaType("application", "json", StandardCharsets.UTF_8),
                        MediaType.APPLICATION_JSON));
            }
        }
    }
}
