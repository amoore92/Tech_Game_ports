package com.techgame.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class GameDataTest {
    private static final String T = "NAME needs DRUG";
    private static final String N = "Ava, Stone";
    private static final String D = "Generic0,Brand0,Class0,Indication0,Effect0,Warning0";

    @Test void GD01_parsePools_valid() {
        Pools p = GameData.parsePools(T, N, D);
        assertEquals(List.of(T), p.templates());
        assertEquals(List.of("Ava"), p.names().firstNames());
        assertEquals(List.of("Stone"), p.names().lastNames());
        assertEquals(1, p.drugs().size());
    }
    @Test void GD02_parsePools_nullTemplate() { assertThrows(NullPointerException.class, () -> GameData.parsePools(null, N, D)); }
    @Test void GD03_parsePools_nullNames() { assertThrows(NullPointerException.class, () -> GameData.parsePools(T, null, D)); }
    @Test void GD04_parsePools_nullDrugs() { assertThrows(NullPointerException.class, () -> GameData.parsePools(T, N, null)); }
    @Test void GD05_parsePools_empty() {
        Pools p = GameData.parsePools("", "", "");
        assertTrue(p.templates().isEmpty()); assertTrue(p.drugs().isEmpty());
        assertTrue(p.names().firstNames().isEmpty()); assertTrue(p.names().lastNames().isEmpty());
    }
    @Test void GD06_toLines_emptyOrWhitespace() {
        assertEquals(List.of(), GameData.toLines(""));
        assertEquals(List.of(), GameData.toLines(" \n\t "));
    }
    @Test void GD07_toLines_bomAndWindowsNewlines() {
        assertEquals(List.of("A", "B"), GameData.toLines("\uFEFF A\r\n\r\n B \r\n"));
    }
    @Test void GD08_toLines_null() { assertThrows(NullPointerException.class, () -> GameData.toLines(null)); }
    @Test void GD09_parseTemplates_filter() {
        assertEquals(List.of(T), GameData.parseTemplates(T + "\nNAME only\nDRUG only\nname DRUG\n"));
    }
    @Test void GD10_parseTemplates_empty() { assertEquals(List.of(), GameData.parseTemplates("no placeholders")); }
    @Test void GD11_parseTemplates_null() { assertThrows(NullPointerException.class, () -> GameData.parseTemplates(null)); }
    @Test void GD12_parseNames_twoValid() {
        Names n = GameData.parseNames("Ava, Stone\nMia, Reed");
        assertEquals(List.of("Ava", "Mia"), n.firstNames());
        assertEquals(List.of("Stone", "Reed"), n.lastNames());
    }
    @Test void GD13_parseNames_missingLast() {
        Names n = GameData.parseNames("Ava");
        assertEquals(List.of("Ava"), n.firstNames()); assertTrue(n.lastNames().isEmpty());
    }
    @Test void GD14_parseNames_missingFirst() {
        Names n = GameData.parseNames(", Stone");
        assertTrue(n.firstNames().isEmpty()); assertEquals(List.of("Stone"), n.lastNames());
    }
    @Test void GD15_parseNames_empty() {
        Names n = GameData.parseNames("");
        assertTrue(n.firstNames().isEmpty()); assertTrue(n.lastNames().isEmpty());
    }
    @Test void GD16_parseNames_null() { assertThrows(NullPointerException.class, () -> GameData.parseNames(null)); }
    @Test void GD17_parseCsvLine_normalAndEmpty() {
        assertEquals(List.of("A", "B"), GameData.parseCsvLine(" A , B "));
        assertEquals(List.of(""), GameData.parseCsvLine(""));
    }
    @Test void GD18_parseCsvLine_quotedComma() {
        assertEquals(List.of("A", "B,C", "D"), GameData.parseCsvLine("A,\"B,C\",D"));
    }
    @Test void GD19_parseCsvLine_doubledQuote() {
        assertEquals(List.of("A\"B", "C"), GameData.parseCsvLine("\"A\"\"B\",C"));
    }
    @Test void GD20_parseCsvLine_null() { assertThrows(NullPointerException.class, () -> GameData.parseCsvLine(null)); }
    @Test void GD21_parseCsvLine_unterminatedQuoteObservedBehavior() {
        // The implementation does NOT reject unterminated quotes; document this as observed behavior.
        assertEquals(List.of("A,B"), GameData.parseCsvLine("\"A,B"));
    }
    @Test void GD22_parseDrugs_headerAndValid() {
        List<Drug> drugs = GameData.parseDrugs("Generic Name,Brand Name,Class,Indications,Side Effects,Warnings\n" + D);
        assertEquals(1, drugs.size()); assertEquals("Brand0", drugs.get(0).brand());
    }
    @Test void GD23_parseDrugs_missingRequiredExcluded() {
        assertTrue(GameData.parseDrugs(",Brand0,Class0,Indication0,Effect0,Warning0").isEmpty());
        assertTrue(GameData.parseDrugs("Generic0,,Class0,Indication0,Effect0,Warning0").isEmpty());
    }
    @Test void GD24_parseDrugs_missingOptionalFieldsDefaultEmpty() {
        Drug d = GameData.parseDrugs("Generic0,Brand0").get(0);
        assertEquals("", d.drugClass()); assertEquals("", d.warnings());
    }
    @Test void GD25_parseDrugs_empty() { assertTrue(GameData.parseDrugs("").isEmpty()); }
    @Test void GD26_parseDrugs_null() { assertThrows(NullPointerException.class, () -> GameData.parseDrugs(null)); }
    @Test void GD27_parseDrugs_bomHeader() {
        assertEquals(1, GameData.parseDrugs("\uFEFFGeneric Name,Brand Name\n" + D).size());
    }
    @Test void GD29_parseNames_commaOnlyObservedFailure() {
        // A comma-only line has no first component after Java split drops trailing empties.
        assertThrows(ArrayIndexOutOfBoundsException.class, () -> GameData.parseNames(","));
    }
    @Test void GD30_field_nullList_viaReflection() throws Exception {
        var m = GameData.class.getDeclaredMethod("field", List.class, int.class);
        m.setAccessible(true);
        var ex=assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> m.invoke(null, null, 0));
        assertInstanceOf(NullPointerException.class, ex.getCause());
    }
    @Test void GD31_toLines_oneCharacter() {
        assertEquals(List.of("A"), GameData.toLines("A"));
    }
    @Test void GD32_parseCsvLine_noComma() {
        assertEquals(List.of("A"), GameData.parseCsvLine("A"));
    }
    @Test void GD28_field_indexBoundary_viaReflection() throws Exception {
        // Private helper: direct reflection is used for the otherwise unreachable index boundaries.
        var m = GameData.class.getDeclaredMethod("field", List.class, int.class);
        m.setAccessible(true);
        assertEquals("A", m.invoke(null, List.of("A", "B"), 0));
        assertEquals("B", m.invoke(null, List.of("A", "B"), 1));
        assertEquals("", m.invoke(null, List.of("A", "B"), 2));
        var ex = assertThrows(java.lang.reflect.InvocationTargetException.class,
                () -> m.invoke(null, List.of("A", "B"), -1));
        assertInstanceOf(IndexOutOfBoundsException.class, ex.getCause());
    }
}
