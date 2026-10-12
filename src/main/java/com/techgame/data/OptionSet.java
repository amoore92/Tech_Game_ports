package com.techgame.data;

import java.util.List;

/** The correct value for one build step plus the (already shuffled) options shown for it. */
public record OptionSet(String correct, List<String> options)
{
}
