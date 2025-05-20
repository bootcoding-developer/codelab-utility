package com.bootcoding.aws_ec2_notes_generator.app;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoteRequest {
    private String Content;
    private String tone;
}
