package net.akehurst.application.framework.engineering.datatype.transform.hjson;

import net.akehurst.application.framework.common.annotations.datatype.DataType;

@DataType
public class Datatype1 {

    private String string;

    public Datatype1() {}

    public String getString() {
        return this.string;
    }

    public void setString(final String value) {
        this.string = value;
    }

}
