
package com.vehicleservice.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class ComplaintAnalysisService {

    public String analyze(String complaint) {
        if (complaint == null || complaint.isBlank()) {
            return "OTHER";
        }

        String text = complaint.toLowerCase(Locale.ROOT);

        if (containsAny(text, "brake", "braking", "brakes")) {
            return "BRAKES";
        }

        if (containsAny(text, "battery", "won't start",
                "wont start", "not starting", "self start")) {
            return "BATTERY";
        }

        if (containsAny(text, "engine", "motor", "overheating")) {
            return "ENGINE";
        }

        if (containsAny(text, "tyre", "tire", "puncture", "wheel")) {
            return "TYRES";
        }

        if (containsAny(text, "chain", "sprocket")) {
            return "CHAIN";
        }

        return "OTHER";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
