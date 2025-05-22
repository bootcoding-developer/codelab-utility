package com.bootcoding.aws_ec2_notes_generator.app;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/aws-ec2-note")
@AllArgsConstructor
public class NoteGeneratorController {

    private final NoteGeneratorService noteGeneratorService;

    @PostMapping("/generate")
    public ResponseEntity<String> generateNote(@RequestBody NoteRequest noteRequest) {
        String response = noteGeneratorService.generateNote(noteRequest);
        return ResponseEntity.ok(response);
    }
}
