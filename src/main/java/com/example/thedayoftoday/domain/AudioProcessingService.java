package com.example.thedayoftoday.domain;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class AudioProcessingService {

    private static final String PYTHON_EXECUTABLE = "python";
    private static final String SCRIPT_PATH = "C:\\Users\\doosa\\Desktop\\대학\\졸설\\WhisperSTT\\stt_script.py";

    public String processAudio(String filePath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTABLE, SCRIPT_PATH, filePath);
            Process process = pb.start();

            // Python 스크립트의 출력을 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.lines().collect(Collectors.joining());

            // 프로세스 종료 대기
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return result;
            } else {
                return "{\"error\":\"Python script execution failed\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"Exception occurred: " + e.getMessage() + "\"}";
        }
    }
}
