package Codify.parsing.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {
    private final AmazonS3 amazonS3Client;

    @Value("${cloud.aws.s3.bucket}") // Spring의 설정 파일(yml)에서 값을 읽어오는 어노테이션
    private String bucket;


    /**
     * S3에서 s3Key로 파일 내용을 읽어오는 메소드
     * @param s3Key S3에 저장된 실제 키 (예: "submissions/2024/assignment1/student123/code.cpp")
     * @return 파일의 텍스트 내용
     */
    public String readFileFromS3ByKey(String s3Key) {
        try {
            log.info("S3에서 파일 읽기 시도 - Bucket: {}, Key: {}", bucket, s3Key);
            
            // 파일 존재 여부 먼저 확인
            if (!amazonS3Client.doesObjectExist(bucket, s3Key)) {
                log.error("S3에 파일이 존재하지 않습니다 - Bucket: {}, Key: {}", bucket, s3Key);
                throw new RuntimeException("S3에 파일이 존재하지 않습니다: " + s3Key);
            }
            
            S3Object s3Object = amazonS3Client.getObject(bucket, s3Key);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();
            
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            
            return content.toString();
        } catch (IOException e) {
            throw new RuntimeException("S3에서 파일을 읽는 중 오류 발생: " + s3Key, e);
        }
    }
}
