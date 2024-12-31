package com.example.evatgbotmanager.controller;

import com.example.evatgbotmanager.dto.RegisterTelegramDto;
import com.example.evatgbotmanager.entity.TelegramBotEntity;
import com.example.evatgbotmanager.service.TelegramService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController("telegram")
@AllArgsConstructor
public class TelegramController {

    private final TelegramService telegramService;

    @PostMapping(path = "/register")
    public ResponseEntity<String> registerTelegram(@RequestBody RegisterTelegramDto registerTelegramDto) {
        TelegramBotEntity entity = telegramService.saveBot(registerTelegramDto);
        telegramService.runBot(entity);
        return ResponseEntity.ok("success");
    }

    @GetMapping(path = "/registered-bots")
    public ResponseEntity<List<TokenDescription>> findAll() {
        return ResponseEntity.ok(telegramService.findAll().stream().map(s -> new TokenDescription(s.getDomainBot(), s.getUrl())).collect(Collectors.toList()));
    }
    @PostMapping(path = "/stop-bot")
    public ResponseEntity<String> stopBot(@RequestParam String botName) {
        String statusDesc = telegramService.stopBot(botName);
        return ResponseEntity.ok(statusDesc);
    }

    @DeleteMapping(path = "/delete")
    public ResponseEntity<String> delete(@RequestParam String botName) {
        String statusDesc = telegramService.deleteBot(botName);
        return ResponseEntity.ok(statusDesc);
    }

    private record TokenDescription(String name, String url) {}
}
