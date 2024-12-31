package com.example.evatgbotmanager.controller;

import com.example.evatgbotmanager.dto.AnalyticMetaData;
import com.example.evatgbotmanager.entity.PartnerPostBackMappingEntity;
import com.example.evatgbotmanager.service.AnalyticServiceManager;
import com.example.evatgbotmanager.service.PartnerPostbackParserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.InetAddress;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("partnerevents")
@RequiredArgsConstructor
public class PartnerEventController {
    private final PartnerPostbackParserService partnerPostbackParserService;
    private final AnalyticServiceManager analyticServiceManager;

    @Operation(summary = "Store event from partner service (postback)")
    @GetMapping("/storeEvent") // GET because service integration doesn't support other http methods
    public ResponseEntity<String> registerEvent(
            @RequestParam Map<String, String> allRequestParams,
            @RequestHeader Map<String, String> headers) {
        log.debug("Received params from postback {} \n headers: {}", allRequestParams, headers);
        String host = getHost(headers);
        String domain = host.substring(4);
        PartnerPostBackMappingEntity partnerPostbackMapping = partnerPostbackParserService.getPartnerPostbackMapping(domain);
        if (partnerPostbackMapping == null) {
            log.warn("Postback params mappping not found by domain {}", domain);
            return ResponseEntity.ok("not found mapping");
        }
        var analyticMetaData = new AnalyticMetaData(
                domain,
                parseActionType(allRequestParams, partnerPostbackMapping),
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getClickId())).orElse(""),
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getTraderId())).orElse(""),
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getTgUserId())).orElse(""),
                "",
                InetAddress.getLoopbackAddress().getHostAddress(),
                Map.of(),
                allRequestParams

        );
        analyticServiceManager.sendEvent(analyticMetaData);
        return ResponseEntity.ok("success");
    }

    private String parseActionType(Map<String, String> allRequestParams, PartnerPostBackMappingEntity partnerPostbackMapping) {
        String actionRegValue = allRequestParams.get(partnerPostbackMapping.getRegistration());
        String actionFtdValue = allRequestParams.get(partnerPostbackMapping.getFistReplenishment());
        if (StringUtils.isBlank(actionRegValue) && StringUtils.isBlank(actionFtdValue)) {
            return "unsupported_partner_event";
        }
        if (Boolean.parseBoolean(actionFtdValue) || "1".equalsIgnoreCase(actionFtdValue)) {
            return "ftd";
        }
        if (Boolean.parseBoolean(actionRegValue) || "1".equalsIgnoreCase(actionRegValue)) {
            return "reg";
        }
        return "unsupported_partner_event";
    }

    @Operation(summary = "Store event from partner service (postback)")
    @GetMapping("/storeEvent/{action}") // GET because service integration doesn't support other http methods
    public ResponseEntity<String> registerEventForAction(
            @PathVariable String action,
            @RequestParam Map<String, String> allRequestParams,
            @RequestHeader Map<String, String> headers) {
        log.debug("Received params from postback {} \n headers: {}", allRequestParams, headers);
        String host = getHost(headers);
        String domain = host.substring(4);
        PartnerPostBackMappingEntity partnerPostbackMapping = partnerPostbackParserService.getPartnerPostbackMapping(domain);
        if (partnerPostbackMapping == null) {
            log.warn("Postback params mappping not found by domain {}", domain);
            return ResponseEntity.ok("not found mapping");
        }
        var analyticMetaData = new AnalyticMetaData(
                domain,
                action,
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getClickId())).orElse(""),
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getTraderId())).orElse(""),
                Optional.ofNullable(allRequestParams.get(partnerPostbackMapping.getTgUserId())).orElse(""),
                "",
                InetAddress.getLoopbackAddress().getHostAddress(),
                Map.of(),
                allRequestParams

        );
        analyticServiceManager.sendEvent(analyticMetaData);
        return ResponseEntity.ok("success");
    }

    private String getHost(Map<String, String> headers) {
        return Optional.ofNullable(headers.get(HttpHeaders.HOST))
                .or(() -> Optional.ofNullable(headers.get(HttpHeaders.HOST.toLowerCase())))
                .or(() -> Optional.ofNullable(headers.get(HttpHeaders.HOST.toUpperCase())))
                .filter(host -> {
                    if (host.startsWith("api.")) {
                        return true;
                    } else {
                        log.error("Incorrect host from postback. Host Should start with api. {}", host);
                        return false;
                    }
                })
                .orElseThrow(UnsupportedAddressTypeException::new);
    }
}
