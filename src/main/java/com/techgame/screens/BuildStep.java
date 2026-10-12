package com.techgame.screens;

import com.techgame.SceneKey;
import com.techgame.data.OptionSet;
import com.techgame.data.Progress;
import com.techgame.data.Round;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * The four steps of building a drug to hand over. They all play the same way (see
 * {@link BuildStepScreen}) and differ only in this configuration.
 */
public enum BuildStep
{
    //  Pick the correct drug for the patient's greeting.
    DRUG("Drug", Round::drugOptions, (p, v) -> p.picked = v,
            Round::greetingText, SceneKey.LABEL_INDICATIONS),
    INDICATIONS("for", Round::indicationOptions, (p, v) -> p.indications = v,
            BuildStep::labelFor, SceneKey.LABEL_SIDE_EFFECTS),
    SIDE_EFFECTS("Notable side effects", Round::sideEffectOptions, (p, v) -> p.sideEffects = v,
            BuildStep::labelFor, SceneKey.LABEL_WARNINGS),
    WARNINGS("Warnings and Counseling", Round::warningOptions, (p, v) -> p.warnings = v,
            BuildStep::labelFor, SceneKey.GAME);

    /** Paper heading. */
    public final String heading;
    /** The { correct, options } set for this step. */
    public final Function<Round, OptionSet> options;
    /** Records the placed value on success. */
    public final BiConsumer<Progress, String> record;
    /** Line above the paper. */
    public final Function<Round, String> context;
    /** Where to go on success. */
    public final SceneKey next;

    BuildStep (String heading, Function<Round, OptionSet> options, BiConsumer<Progress, String> record,
               Function<Round, String> context, SceneKey next)
    {
        this.heading = heading;
        this.options = options;
        this.record = record;
        this.context = context;
        this.next = next;
    }

    //  Which drug we're labelling -- shown on the label steps (already picked).
    private static String labelFor (Round round)
    {
        return "Label for: " + round.drug().brand() + " (" + round.drug().generic() + ")";
    }
}
