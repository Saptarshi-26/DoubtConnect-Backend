package com.saptarshi.doubtconnect.dto;

import lombok.Data;
import org.springframework.transaction.annotation.Transactional;

@Data
@Transactional
public class UpdateSessionDTO {
    private String subject;
    private String description;
}