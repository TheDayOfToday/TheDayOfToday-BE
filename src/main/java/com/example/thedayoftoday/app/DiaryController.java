package com.example.thedayoftoday.app;

import com.example.thedayoftoday.domain.dto.DiaryRequestDto;
import com.example.thedayoftoday.domain.dto.DiaryResponseDto;
import com.example.thedayoftoday.domain.entity.Diary;
import com.example.thedayoftoday.domain.service.DiaryService;
import jakarta.validation.Valid;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/diary")
public class DiaryController {

    private final DiaryService diaryService;

    public DiaryController(DiaryService diaryService) {
        this.diaryService = diaryService;
    }

    @PostMapping(value = "/create/{userId}")
    public ResponseEntity<DiaryResponseDto> DiaryCreateController(@Valid @RequestBody DiaryRequestDto diaryRequestDto,
                                                                  @PathVariable Long userId) {

        Diary diary = diaryService.createDiary(diaryRequestDto, userId);
        DiaryResponseDto diaryResponseDto = diaryService.getDiaryResponseDto(userId, diary);
        return ResponseEntity.status(HttpStatus.CREATED).body(diaryResponseDto);
    }

    @DeleteMapping("/delete/{diaryId}")
    public ResponseEntity<String> DiaryDeleteController(@PathVariable Long diaryId) {
        diaryService.deleteDiary(diaryId);
        return ResponseEntity.ok("삭제가 완료되었습니다.");
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiaryResponseDto>> findTitleController(
            @RequestParam Long userId,
            @RequestParam String title) {
        List<DiaryResponseDto> findList = diaryService.findByTitle(userId, title);
        return ResponseEntity.ok(findList);
    }
}
