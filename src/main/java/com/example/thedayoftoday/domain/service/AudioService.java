package com.example.thedayoftoday.domain.service;

import com.example.thedayoftoday.domain.dto.AudioResponseDto;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

@Service
public class AudioService {

    private static final String PYTHON_EXECUTABLE = "python";
    private static final String SCRIPT_PATH = "C:\\Users\\doosa\\Desktop\\대학\\졸설\\WhisperSTT\\stt_script.py";

    public AudioResponseDto processAudio(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder(PYTHON_EXECUTABLE, SCRIPT_PATH, file.getAbsolutePath());
            Process process = pb.start();

            // Python 스크립트의 출력을 읽기
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.lines().collect(Collectors.joining());

            // 프로세스 종료 대기
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return AudioResponseDto.success(result);
            } else {
                return AudioResponseDto.failure("Python script execution failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return AudioResponseDto.failure("Exception occurred: " + e.getMessage());
        }
    }
}
