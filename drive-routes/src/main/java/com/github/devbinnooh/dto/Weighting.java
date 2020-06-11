package com.github.devbinnooh.dto;

/**
 * Determines the way the ''best'' route is calculated. Default is fastest.
 * Other options are shortest (e.g. for vehicle=foot or bike) and short_fastest which finds a reasonable balance between shortest and fastest.
 * Requires ch.disable=true.
 */
public enum Weighting {

    FASTEST,SHORTEST,SHORT_FASTEST;
}
