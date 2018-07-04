package net.akehurst.application.framework.engineering.datatype.transform.hjson;

import java.util.List;
import java.util.Objects;

import org.hjson.JsonArray;
import org.hjson.JsonObject;
import org.hjson.JsonValue;
import org.jooq.lambda.Seq;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.Datatype2HJsonObject;
import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.List2HJsonArray;
import net.akehurst.application.framework.engineering.datatype.transform.hjson.rules.String2HJsonValue;

public class test_Transformer {

    Transformer sut;

    @Before
    public void setup() {
        this.sut = new Transformer();
    }

    @Test
    public void left2Right_String() {
        final String left = "Hello World!";
        final JsonValue right = this.sut.transformLeft2Right(String2HJsonValue.class, left);

        Assert.assertEquals(left, right.asString());
    }

    @Test
    public void right2Left_String() {
        final JsonValue right = JsonValue.valueOf("Hello World!");
        final String left = this.sut.transformRight2Left(String2HJsonValue.class, right);

        Assert.assertEquals(left, right.asString());
    }

    @Test
    public void left2Right_ListOfString() {
        final List<String> left = Seq.of("Hello", "World", "!").toList();
        final JsonArray right = this.sut.transformLeft2Right(List2HJsonArray.class, left);

        Assert.assertEquals(left.size(), right.size());
        Seq.crossJoin(Seq.seq(left), Seq.seq(right.values())).allMatch(t -> Objects.equals(t.v1(), t.v2()));
    }

    @Test
    public void ledt2Right_Datatype1() {
        final Datatype1 left = new Datatype1();
        left.setString("Hello World!");

        final JsonObject right = this.sut.transformLeft2Right(Datatype2HJsonObject.class, left);

        Assert.assertEquals(left.getString(), right.get("string").asString());
    }
}
