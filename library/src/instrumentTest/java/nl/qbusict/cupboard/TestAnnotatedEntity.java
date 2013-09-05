package nl.qbusict.cupboard;

import nl.qbusict.cupboard.annotation.Column;

public class TestAnnotatedEntity {
    public Long _id;

    public String myStringValue;
    @Column("data1")
    public String renamedStringValue;
}
