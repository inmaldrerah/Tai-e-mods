package sa.pta.element;

/**
 * Represents an instance store: base.field = from.
 */
public class InstanceStore {

    private final Variable base;

    private final Field field;

    private final Variable from;

    public InstanceStore(Variable base, Field field, Variable from) {
        this.base = base;
        this.field = field;
        this.from = from;
    }

    public Variable getBase() {
        return base;
    }

    public Field getField() {
        return field;
    }

    public Variable getFrom() {
        return from;
    }
}
