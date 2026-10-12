package com.techgame.data;

import java.util.List;

/** Independent first/last name pools so any first name can be mixed with any last name. */
public record Names(List<String> firstNames, List<String> lastNames)
{
}
