package com.techgame.data;

/**
 * One patient at the counter: the greeting (showing one of the drug's identifiers),
 * the drug-pick choices, and the three label option sets.
 *
 * @param personKey   which customer figure stands at the counter
 * @param answerField the drug-identity field the player must pick (never the one shown)
 */
public record Round(
        Drug drug,
        DrugField shownField,
        String shownValue,
        String patientName,
        String greetingText,
        String personKey,
        DrugField answerField,
        String drugCorrect,
        OptionSet drugOptions,
        OptionSet indicationOptions,
        OptionSet sideEffectOptions,
        OptionSet warningOptions)
{
}
