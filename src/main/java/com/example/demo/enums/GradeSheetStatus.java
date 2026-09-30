package com.example.demo.enums;

import java.util.Locale;

public enum GradeSheetStatus {

    DRAFT("DRAFT"),
    SUBMITTED("SUBMITTED"),
    RETURNED("RETURNED"),
    REVIEWED("REVIEWED"),
    PUBLISHED("PUBLISHED");

    private final String code;

    GradeSheetStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static GradeSheetStatus fromCode(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        try {
            return GradeSheetStatus.valueOf(
                    code.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}