package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryAllRequestDto;
import com.example.thedayoftoday.domain.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<DiaryAllRequestDto> createDiary(
            @Valid @RequestBody DiaryAllRequestDto diaryRequestDto,
            @PathVariable Long userId) {

        DiaryAllRequestDto diaryResponseDto = diaryService.createDiary(diaryRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    @DeleteMapping("/delete/{diaryId}")
    public ResponseEntity<String> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }
}
