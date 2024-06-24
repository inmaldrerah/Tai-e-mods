package pascal.taie.language.classes;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pascal.taie.language.classes.Pattern.*;

public class PatternTest {

    private static NamePattern NP(String... nps) {
        return new NamePattern(Stream.of(nps)
                .map(np -> switch (np) {
                    case FULLNAME_WILDCARD_MARK -> FULLNAME_WILDCARD;
                    case NAME_WILDCARD_MARK -> NAME_WILDCARD;
                    default -> new StringUnit(np);
                })
                .toList());
    }

    @Test
    void testNamePattern() {
        assertEquals(NP("**"), parseNamePattern("**"));
        assertEquals(NP("*"), parseNamePattern("*"));
        assertEquals(NP("ABC"), parseNamePattern("ABC"));
        assertEquals(NP("com", "**", "X"), parseNamePattern("com**X"));
        assertEquals(NP("com.example.", "*"), parseNamePattern("com.example.*"));
        assertEquals(NP("com.example.", "*", ".abc.", "**"), parseNamePattern("com.example.*.abc.**"));
        assertEquals(NP("com.example.", "**", ".abc.", "*"), parseNamePattern("com.example.**.abc.*"));
        assertEquals(NP("com.example.", "**", ".abc.", "*", ".def"), parseNamePattern("com.example.**.abc.*.def"));
    }

    /**
     * Class pattern, without subclasses.
     */
    private static ClassPattern CP1(String... nps) {
        return new ClassPattern(NP(nps), false);
    }

    /**
     * Class pattern, including subclasses.
     */
    private static ClassPattern CP2(String... nps) {
        return new ClassPattern(NP(nps), true);
    }

    @Test
    void testClassPattern() {
        assertEquals(CP1("com.example.", "*"), ofC("com.example.*"));
        assertEquals(CP1("com", "**", "X"), ofC("com**X"));
        assertEquals(CP1("com.example.", "*", ".abc.", "**"), ofC("com.example.*.abc.**"));
        assertEquals(CP1("com.example.", "**", ".abc.", "*"), ofC("com.example.**.abc.*"));
        assertEquals(CP1("com.example.", "**", ".abc.", "*", ".def"), ofC("com.example.**.abc.*.def"));
        assertEquals(CP2("com", "**", "X"), ofC("com**X^"));
        assertEquals(CP2("com.example.", "*", ".abc.", "**"), ofC("com.example.*.abc.**^"));
        assertEquals(CP2("com.example.", "**", ".abc.", "*"), ofC("com.example.**.abc.*^"));
        assertEquals(CP2("com.example.", "**", ".abc.", "*", ".def"), ofC("com.example.**.abc.*.def^"));
    }

    /**
     * Type pattern, without subtypes.
     */
    private static TypePattern TP1(String... nps) {
        return new TypePattern(NP(nps), false);
    }

    /**
     * Type pattern, including subtypes.
     */
    private static TypePattern TP2(String... nps) {
        return new TypePattern(NP(nps), true);
    }

    @Test
    void testMethodPattern() {
        assertEquals(
                new MethodPattern(
                        CP1("com.example.", "*"),
                        TP1("int"),
                        NP("foo"),
                        List.of(TP1("java.lang.String"), TP1("int"))
                ),
                ofM("<com.example.*: int foo(java.lang.String,int)>")
        );
        assertEquals(
                new MethodPattern(
                        CP1("com.example.", "*"),
                        TP1("int"),
                        NP("foo"),
                        List.of(TP1("java.lang.String"), PARAM_WILDCARD)
                ),
                ofM("<com.example.*: int foo(java.lang.String,~)>")
        );
        assertEquals(
                new MethodPattern(
                        CP1("com.example.", "*"),
                        TP1("int"),
                        NP("foo", "*"),
                        List.of(TP1("java.lang.String"), PARAM_WILDCARD,
                                TP1("int"), PARAM_WILDCARD)
                ),
                ofM("<com.example.*: int foo*(java.lang.String,~,int,~)>")
        );
        assertEquals(
                new MethodPattern(
                        CP1("com.example.", "*"),
                        TP1("void"),
                        NP("foo", "*"),
                        List.of(TP2("java.util.Collection"), PARAM_WILDCARD,
                                TP1("java.lang.String"), PARAM_WILDCARD)
                ),
                ofM("<com.example.*: void foo*(java.util.Collection^,~,java.lang.String,~)>")
        );
    }

    @Test
    void testFieldPattern() {
        assertEquals(
                new FieldPattern(
                        CP1("com.example.", "*"),
                        TP1("int"),
                        NP("field", "*")
                ),
                ofF("<com.example.*: int field*>")
        );
        assertEquals(
                new FieldPattern(
                        CP1("com", "**", "X"),
                        TP1("int"),
                        NP("field1")
                ),
                ofF("<com**X: int field1>")
        );
        assertEquals(
                new FieldPattern(
                        CP1("com", "**", "X"),
                        TP2("java.util.Collection"),
                        NP("field1")
                ),
                ofF("<com**X: java.util.Collection^ field1>")
        );
        assertEquals(
                new FieldPattern(
                        CP2("com", "**", "X"),
                        TP2("java.util.Collection"),
                        NP("field1")
                ),
                ofF("<com**X^: java.util.Collection^ field1>")
        );
        assertEquals(
                new FieldPattern(
                        CP2("com", "**", "X"),
                        TP2("com.example.", "*"),
                        NP("field2")
                ),
                ofF("<com**X^: com.example.*^ field2>")
        );
        assertEquals(
                new FieldPattern(
                        CP1("com.example.", "*"),
                        TP1("void"),
                        NP("field2")
                ),
                ofF("<com.example.*: void field2>")
        );
    }
}
