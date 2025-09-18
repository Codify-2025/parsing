package Codify.parsing.controller;

import Codify.parsing.domain.Submission;
import Codify.parsing.dto.CodeDto;
import Codify.parsing.dto.ResultDto;
import Codify.parsing.repository.SubmissionRepository;
import Codify.parsing.service.ParsingService;
import Codify.parsing.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/parse")
public class ParsingController {
    private final ParsingService parsingService;
    private final S3Service s3Service;
    private final SubmissionRepository submissionRepository;

    @PostMapping("/")
    public ResponseEntity<ResultDto> parsing(@RequestBody CodeDto codeDto) {
        return ResponseEntity.ok(parsingService.parsing(codeDto));
    }

    @GetMapping("/file/{fileName}")
    public ResponseEntity<ResultDto> parseFromFile(@PathVariable String fileName) throws IOException {
        return ResponseEntity.ok(parsingService.parseFromFile(fileName));
    }


    //claude

    //file읽어 오는 로직
    @GetMapping("/s3/content/by-filename")
    public ResponseEntity<String>getFileContentFromS3ByFileName(@RequestParam String fileName) {
        Submission submission = submissionRepository.findByFileName(fileName)
                .orElseThrow(() -> new  RuntimeException("파일 정보를 찾을 수 없습니다: " + fileName));
        String content = s3Service.readFileFromS3ByKey(submission.getS3Key());
        return ResponseEntity.ok(content);
    }

}
