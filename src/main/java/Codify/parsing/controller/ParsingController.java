package Codify.parsing.controller;

import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.service.ParsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/parse")
public class ParsingController {
    private final ParsingService parsingService;

    @PostMapping("/")
    public ResponseEntity<ResultDto> parsing(@RequestBody CodeDto codeDto) {
        return ResponseEntity.ok(parsingService.parsing(codeDto));
    }

}
