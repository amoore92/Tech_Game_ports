package com.techgame.data;

/** A column of the flashcard data (the JS version used the property name as a string key). */
public enum DrugField
{
    GENERIC, BRAND, DRUG_CLASS, INDICATIONS, SIDE_EFFECTS, WARNINGS;

    public String of (Drug drug)
    {
        return switch (this)
        {
            case GENERIC -> drug.generic();
            case BRAND -> drug.brand();
            case DRUG_CLASS -> drug.drugClass();
            case INDICATIONS -> drug.indications();
            case SIDE_EFFECTS -> drug.sideEffects();
            case WARNINGS -> drug.warnings();
        };
    }
}
