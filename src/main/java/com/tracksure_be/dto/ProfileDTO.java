package com.tracksure_be.dto;

import com.tracksure_be.enums.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDTO {
    private Long profileId;
    private String fullName;
    private String rollNumber;
    private Department department;
    private Integer batch;
    private String bio;
    private String profilePic;
    private String coverPic;
}