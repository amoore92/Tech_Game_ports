package com.techgame.data;

import com.badlogic.gdx.math.MathUtils;
import com.techgame.Assets;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Deals rounds from the pools. */
public final class RoundFactory
{
    private static final List<DrugField> SHOWN_FIELDS =
            List.of(DrugField.GENERIC, DrugField.BRAND, DrugField.DRUG_CLASS);
    private static final List<DrugField> NAME_FIELDS = List.of(DrugField.GENERIC, DrugField.BRAND);
    private static final int DISTRACTORS = 4;

    private RoundFactory ()
    {
    }

    /** Builds a complete round. Called at load time and after every hand-in / reset. */
    public static Round makeRound (Pools pools)
    {
        //  The answer drug for this round.
        Drug drug = pick(pools.drugs());

        //  Patient name: random first + random last, from the independent pools.
        String patientName = pick(pools.names().firstNames()) + " " + pick(pools.names().lastNames());

        //  Greeting shows ONE of the first three columns.
        DrugField shownField = pick(SHOWN_FIELDS);
        String shownValue = shownField.of(drug);

        //  Fill the template. replace() handles a token appearing more than once.
        String greetingText = pick(pools.templates())
                .replace("NAME", patientName)
                .replace("DRUG", shownValue);

        //  The drug-identity card the player must pick: a DIFFERENT name field than
        //  the one shown. brand<->generic when a name is shown; either when the
        //  class is shown.
        DrugField answerField = switch (shownField)
        {
            case GENERIC -> DrugField.BRAND;
            case BRAND -> DrugField.GENERIC;
            default -> pick(NAME_FIELDS);
        };

        List<Drug> eligible = eligibleDistractorDrugs(pools.drugs(), drug);

        return new Round(
                drug,
                shownField,
                shownValue,
                patientName,
                greetingText,
                pick(Assets.PERSON_KEYS),
                answerField,
                answerField.of(drug),
                buildOptions(answerField, drug, eligible),
                buildOptions(DrugField.INDICATIONS, drug, eligible),
                buildOptions(DrugField.SIDE_EFFECTS, drug, eligible),
                buildOptions(DrugField.WARNINGS, drug, eligible));
    }

    /** Fisher-Yates shuffle (returns a new list, leaves the input alone). */
    public static <T> List<T> shuffle (List<T> list)
    {
        List<T> result = new ArrayList<>(list);
        for (int i = result.size() - 1; i > 0; i--)
        {
            int j = MathUtils.random(i);
            T tmp = result.get(i);
            result.set(i, result.get(j));
            result.set(j, tmp);
        }
        return result;
    }

    private static <T> T pick (List<T> list)
    {
        return list.get(MathUtils.random(list.size() - 1));
    }

    //  Drugs that make safe distractors for a given answer drug: NOT the same class
    //  (so the shown class still points at a single right answer) and NOT the same
    //  generic (so a sibling brand of the same drug is never offered as "wrong").
    private static List<Drug> eligibleDistractorDrugs (List<Drug> drugs, Drug answer)
    {
        List<Drug> eligible = new ArrayList<>();
        for (Drug d : drugs)
        {
            if (!d.drugClass().equals(answer.drugClass()) && !d.generic().equals(answer.generic()))
            {
                eligible.add(d);
            }
        }
        return eligible;
    }

    //  Build a 5-option set for one column: the answer drug's value plus 4 distinct
    //  distractor values pulled from unrelated drugs. De-duplicates by string and
    //  never repeats the correct value.
    private static OptionSet buildOptions (DrugField field, Drug answer, List<Drug> eligible)
    {
        String correct = field.of(answer);
        Set<String> seen = new HashSet<>();
        seen.add(correct);

        List<String> options = new ArrayList<>();
        options.add(correct);
        for (Drug d : shuffle(eligible))
        {
            String value = field.of(d);
            if (value.isEmpty() || !seen.add(value)) continue;
            options.add(value);
            if (options.size() == DISTRACTORS + 1) break;
        }
        return new OptionSet(correct, shuffle(options));
    }
}
