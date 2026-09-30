package com.collablearn.backend.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    private String id;
    private String title;
    private String fileUrl;
    private String fileType;
    private long fileSize;
    private Instant uploadedAt;
}
