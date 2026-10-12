package com.techgame.data;

/**
 * One flashcard row. Multi-value columns keep their '/' separators intact (the
 * label strips use the whole value as one string).
 */
public record Drug(String generic, String brand, String drugClass,
                   String indications, String sideEffects, String warnings)
{
}
