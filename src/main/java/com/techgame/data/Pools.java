package com.techgame.data;

import java.util.List;

/** Everything a new round is dealt from. Parsed once at load time. */
public record Pools(List<String> templates, Names names, List<Drug> drugs)
{
}
