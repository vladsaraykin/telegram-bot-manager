package com.example.evatgbotmanager.config;

import com.example.evatgbotmanager.enums.Analytic;
import com.example.evatgbotmanager.messagesource.DbDelegateMessageSource;
import com.example.evatgbotmanager.repository.TGMessageRepository;
import com.example.evatgbotmanager.repository.TelegramRepository;
import com.example.evatgbotmanager.service.AmplitudeAnalyticService;
import com.example.evatgbotmanager.service.AnalyticServiceManager;
import com.example.evatgbotmanager.service.FacebookAnalyticService;
import com.example.evatgbotmanager.service.TiktokAnalyticService;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Locale;
import java.util.Map;

@EnableScheduling
@Configuration
public class AppConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.create();
    }

    @Bean
    public AmplitudeAnalyticService analyticService(
            WebClient webClient,
            TelegramRepository telegramRepository) {
        return new AmplitudeAnalyticService(webClient, telegramRepository);
    }

    @Bean
    public TiktokAnalyticService tiktokAnalyticService(
            WebClient webClient,
            TelegramRepository telegramRepository) {
        return new TiktokAnalyticService(webClient, telegramRepository);
    }

    @Bean
    public FacebookAnalyticService facebookAnalyticService(
            WebClient webClient,
            TelegramRepository telegramRepository) {
        return new FacebookAnalyticService(webClient, telegramRepository);
    }
    @Bean
    public AnalyticServiceManager analyticServiceManager(
            AmplitudeAnalyticService amplitudeAnalyticService,
            TiktokAnalyticService tiktokAnalyticService,
            FacebookAnalyticService facebookAnalyticService
    ) {
        return new AnalyticServiceManager(
                Map.of(
                        Analytic.AMPLITUDE, amplitudeAnalyticService,
                        Analytic.TITTOK, tiktokAnalyticService,
                        Analytic.FACEBOOK, facebookAnalyticService
                )
        );
    }

    @Bean
    public MessageSource messageSource(TGMessageRepository tgMessageRepository) {
        final ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:messages");
        messageSource.setDefaultLocale(Locale.ENGLISH);
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setDefaultEncoding("UTF-8");

        return new DbDelegateMessageSource(messageSource, tgMessageRepository);
    }
}
