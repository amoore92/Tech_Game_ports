package com.techgame.data;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProgressTest {
    private static Progress complete() {
        Progress p = new Progress(); p.picked="B"; p.indications="I"; p.sideEffects="S"; p.warnings="W";
        return p;
    }
    @Test void PR01_freshIncomplete() { assertFalse(new Progress().isComplete()); }
    @Test void PR02_allPopulatedComplete() { assertTrue(complete().isComplete()); }
    @Test void PR03_noPick() { Progress p=complete(); p.picked=null; assertFalse(p.isComplete()); }
    @Test void PR04_noIndications() { Progress p=complete(); p.indications=null; assertFalse(p.isComplete()); }
    @Test void PR05_noSideEffects() { Progress p=complete(); p.sideEffects=null; assertFalse(p.isComplete()); }
    @Test void PR06_noWarnings() { Progress p=complete(); p.warnings=null; assertFalse(p.isComplete()); }
    @Test void PR07_emptyStringsAreNonNull() {
        Progress p=complete(); p.picked=""; p.indications=""; p.sideEffects=""; p.warnings="";
        assertTrue(p.isComplete()); // Observed behavior; may need stricter intended specification.
    }
}
