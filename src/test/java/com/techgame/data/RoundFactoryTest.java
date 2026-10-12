package com.techgame.data;

import com.techgame.support.TestFixtures;
import org.junit.jupiter.api.Test;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import static org.junit.jupiter.api.Assertions.*;

class RoundFactoryTest {
    private static <T> T privateCall(String name, Class<?>[] parameterTypes, Object... args) {
        try {
            Method m = RoundFactory.class.getDeclaredMethod(name, parameterTypes);
            m.setAccessible(true);
            @SuppressWarnings("unchecked") T result = (T)m.invoke(null, args);
            return result;
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException r) throw r;
            throw new AssertionError(cause);
        } catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private static void checkOptions(OptionSet set, int expectedSize) {
        assertEquals(expectedSize, set.options().size());
        assertEquals(expectedSize, new HashSet<>(set.options()).size());
        assertEquals(1, set.options().stream().filter(set.correct()::equals).count());
    }
    @Test void RF29_makeRound_zeroEligibleDistractors() {
        checkOptions(RoundFactory.makeRound(TestFixtures.pools(1)).drugOptions(),1);
    }
    @Test void RF30_buildOptions_zeroEligibleDistractors() {
        checkOptions(build(DrugField.BRAND, TestFixtures.drug(0), List.of()),1);
    }
    @Test void RF01_makeRound_validPools() {
        Round r = RoundFactory.makeRound(TestFixtures.pools(6));
        assertNotNull(r.drug()); assertEquals("Ava Stone", r.patientName());
        assertFalse(r.greetingText().contains("NAME")); assertFalse(r.greetingText().contains("DRUG"));
        assertEquals(r.answerField().of(r.drug()), r.drugCorrect());
        assertNotEquals(r.shownField(), r.answerField());
        checkOptions(r.drugOptions(), 5); checkOptions(r.indicationOptions(), 5);
        checkOptions(r.sideEffectOptions(), 5); checkOptions(r.warningOptions(), 5);
    }
    @Test void RF02_makeRound_nullPools() { assertThrows(NullPointerException.class, () -> RoundFactory.makeRound(null)); }
    @Test void RF03_makeRound_emptyDrugs() { assertThrows(RuntimeException.class, () -> RoundFactory.makeRound(TestFixtures.pools(0))); }
    @Test void RF04_makeRound_emptyTemplates() {
        Pools p = TestFixtures.pools(6);
        assertThrows(RuntimeException.class, () -> RoundFactory.makeRound(new Pools(List.of(), p.names(), p.drugs())));
    }
    @Test void RF05_makeRound_emptyFirstNames() {
        Pools p = TestFixtures.pools(6);
        assertThrows(RuntimeException.class, () -> RoundFactory.makeRound(new Pools(p.templates(), new Names(List.of(), List.of("Stone")), p.drugs())));
    }
    @Test void RF06_makeRound_emptyLastNames() {
        Pools p = TestFixtures.pools(6);
        assertThrows(RuntimeException.class, () -> RoundFactory.makeRound(new Pools(p.templates(), new Names(List.of("Ava"), List.of()), p.drugs())));
    }
    @Test void RF07_makeRound_threeEligibleDistractors() { checkOptions(RoundFactory.makeRound(TestFixtures.pools(4)).drugOptions(), 4); }
    @Test void RF08_makeRound_fourEligibleDistractors() { checkOptions(RoundFactory.makeRound(TestFixtures.pools(5)).drugOptions(), 5); }
    @Test void RF09_makeRound_fiveEligibleDistractors() { checkOptions(RoundFactory.makeRound(TestFixtures.pools(6)).drugOptions(), 5); }
    @Test void RF10_shuffle_empty() { assertEquals(List.of(), RoundFactory.shuffle(List.of())); }
    @Test void RF11_shuffle_one() { assertEquals(List.of("A"), RoundFactory.shuffle(List.of("A"))); }
    @Test void RF12_shuffle_twoPreservesElementsAndOriginal() {
        List<String> input = new ArrayList<>(List.of("A", "B"));
        List<String> result = RoundFactory.shuffle(input);
        assertNotSame(input, result); assertEquals(List.of("A", "B"), input);
        assertEquals(Set.of("A", "B"), new HashSet<>(result)); assertEquals(2, result.size());
    }
    @Test void RF13_shuffle_null() { assertThrows(NullPointerException.class, () -> RoundFactory.shuffle(null)); }
    @Test void RF14_pick_singleton() {
        assertEquals("A", privateCall("pick", new Class<?>[]{List.class}, List.of("A")));
    }
    @Test void RF15_pick_multipleMember() {
        String result = privateCall("pick", new Class<?>[]{List.class}, List.of("A", "B"));
        assertTrue(List.of("A", "B").contains(result));
    }
    @Test void RF16_pick_empty() { assertThrows(RuntimeException.class, () -> privateCall("pick", new Class<?>[]{List.class}, List.of())); }
    @Test void RF17_pick_null() { assertThrows(NullPointerException.class, () -> privateCall("pick", new Class<?>[]{List.class}, (Object)null)); }
    @Test void RF18_eligibleDistractorDrugs_filtersClassAndGeneric() {
        Drug answer = TestFixtures.drug(0);
        Drug sameClass = new Drug("DifferentGeneric", "OtherBrand", answer.drugClass(), "I", "E", "W");
        Drug sameGeneric = new Drug(answer.generic(), "OtherBrand", "OtherClass", "I", "E", "W");
        Drug eligible = TestFixtures.drug(1);
        List<Drug> result = privateCall("eligibleDistractorDrugs", new Class<?>[]{List.class, Drug.class},
                List.of(answer, sameClass, sameGeneric, eligible), answer);
        assertEquals(List.of(eligible), result);
    }
    @Test void RF19_eligibleDistractorDrugs_emptyList() {
        List<Drug> result = privateCall("eligibleDistractorDrugs", new Class<?>[]{List.class, Drug.class},
                List.of(), TestFixtures.drug(0));
        assertTrue(result.isEmpty());
    }
    @Test void RF20_eligibleDistractorDrugs_nullList() {
        assertThrows(NullPointerException.class, () -> privateCall("eligibleDistractorDrugs",
                new Class<?>[]{List.class, Drug.class}, null, TestFixtures.drug(0)));
    }
    @Test void RF21_eligibleDistractorDrugs_nullAnswer() {
        assertThrows(NullPointerException.class, () -> privateCall("eligibleDistractorDrugs",
                new Class<?>[]{List.class, Drug.class}, List.of(TestFixtures.drug(1)), null));
    }
    private static OptionSet build(DrugField field, Drug answer, List<Drug> eligible) {
        return privateCall("buildOptions", new Class<?>[]{DrugField.class, Drug.class, List.class}, field, answer, eligible);
    }
    @Test void RF22_buildOptions_threeDistractors() { checkOptions(build(DrugField.BRAND, TestFixtures.drug(0), TestFixtures.drugs(4).subList(1,4)), 4); }
    @Test void RF23_buildOptions_fourDistractors() { checkOptions(build(DrugField.BRAND, TestFixtures.drug(0), TestFixtures.drugs(5).subList(1,5)), 5); }
    @Test void RF24_buildOptions_fiveDistractors() { checkOptions(build(DrugField.BRAND, TestFixtures.drug(0), TestFixtures.drugs(6).subList(1,6)), 5); }
    @Test void RF25_buildOptions_duplicateAndEmptyExcluded() {
        Drug duplicate = new Drug("G9", "Brand1", "C9", "I9", "E9", "W9");
        Drug empty = new Drug("G8", "", "C8", "I8", "E8", "W8");
        checkOptions(build(DrugField.BRAND, TestFixtures.drug(0), List.of(TestFixtures.drug(1), duplicate, empty, TestFixtures.drug(2))), 3);
    }
    @Test void RF26_buildOptions_nullField() { assertThrows(NullPointerException.class, () -> build(null, TestFixtures.drug(0), List.of())); }
    @Test void RF27_buildOptions_nullAnswer() { assertThrows(NullPointerException.class, () -> build(DrugField.BRAND, null, List.of())); }
    @Test void RF28_buildOptions_nullEligible() { assertThrows(NullPointerException.class, () -> build(DrugField.BRAND, TestFixtures.drug(0), null)); }
}
