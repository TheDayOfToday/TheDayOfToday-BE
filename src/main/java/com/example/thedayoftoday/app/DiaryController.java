package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryResponseDto;
import com.example.thedayoftoday.domain.service.AiService;
import com.example.thedayoftoday.domain.service.DiaryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping("/create/{userId}")
    public ResponseEntity<DiaryResponseDto> createDiary(
            @Valid @RequestBody DiaryRequestDto diaryRequestDto,
            @PathVariable Long userId) {

        DiaryResponseDto diaryResponseDto = diaryService.createDiary(diaryRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    @DeleteMapping("/delete/{diaryId}")
    public ResponseEntity<String> deleteDiary(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponseDto>> findDiaryByTitle(
            @RequestParam Long userId,
            @RequestParam String title) {
        List<DiaryResponseDto> findList = diaryService.findByTitle(userId, title);
        return ResponseEntity.ok(findList);
    }
}
