package com.github.devbinnooh.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;

@Data
@Builder
public class SRoute {

    private double distanceInMeters;
    private Duration duration;
}
