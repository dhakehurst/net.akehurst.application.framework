package net.akehurst.application.framework.technology.persistence.filesystem;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.hjson.JsonValue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import net.akehurst.application.framework.common.ApplicationFrameworkException;
import net.akehurst.application.framework.common.IApplicationFramework;
import net.akehurst.application.framework.realisation.ApplicationFramework;
import net.akehurst.application.framework.technology.filesystem.StandardFilesystem;
import net.akehurst.application.framework.technology.interfaceFilesystem.FilesystemException;
import net.akehurst.application.framework.technology.interfaceFilesystem.IFile;

public class test_HJsonFile {

    static enum Colour {
        RED, GREEN, BLUE
    }

    public static class Person {
        String name;
        int age;
        Colour hair;

        public Person() {}

        public Person(final String name, final int age, final Colour hair) {
            this.name = name;
            this.age = age;
            this.hair = hair;
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String value) {
            this.name = value;
        }

        public int getAge() {
            return this.age;
        }

        public void setAge(final int value) {
            this.age = value;
        }

        public Colour getHair() {
            return this.hair;
        }

        public void setHair(final Colour value) {
            this.hair = value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name, this.age, this.hair);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof Person) {
                final Person other = (Person) obj;
                return Objects.equals(this.name, other.name) && Objects.equals(this.age, other.age) && Objects.equals(this.hair, other.hair);
            } else {
                return false;
            }
        }
    }

    private IApplicationFramework af;
    private HJsonFile sut;

    public List<Integer> listIntValueTypeToken;
    public Map<String, Integer> mapStringIntValueTypeToken;
    public Map<Integer, String> mapIntStringValueTypeToken;
    public Map<Colour, String> mapColourStringValueTypeToken;
    public Map<String, Person> mapStringPersonValueTypeToken;

    @Before
    public void setup() throws ApplicationFrameworkException {
        this.af = new ApplicationFramework("af", "af");
        this.af.createServiceInstance("fs", StandardFilesystem.class, "fs");
        this.sut = this.af.createObject(HJsonFile.class, "src/test/resources/testData");

    }

    @Test
    public void getFile() {
        final IFile result = this.sut.getFile();
        Assert.assertTrue(result.exists());
    }

    @Test
    public void getJson() throws IOException, FilesystemException {
        final JsonValue result = this.sut.getJson();
        Assert.assertFalse(result.toString().isEmpty());
    }

    @Test
    public void booleanValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.booleanValue");
        final Set<Boolean> values = this.sut.retrieve(null, Boolean.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(true));
    }

    @Test
    public void shortValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.intValue");
        final Set<Short> values = this.sut.retrieve(null, Short.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains((short) 1));
    }

    @Test
    public void intValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.intValue");
        final Set<Integer> values = this.sut.retrieve(null, Integer.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(1));
    }

    @Test
    public void longValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.intValue");
        final Set<Long> values = this.sut.retrieve(null, Long.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(1l));
    }

    @Test
    public void floatValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.realValue");
        final Set<Float> values = this.sut.retrieve(null, Float.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(3.14f));
    }

    @Test
    public void doubleValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.realValue");
        final Set<Double> values = this.sut.retrieve(null, Double.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(3.14));
    }

    @Test
    public void stringValue() {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "primitives.stringValue");
        final Set<String> values = this.sut.retrieve(null, String.class, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains("hello world"));
    }

    @Test
    public void listIntValue() throws NoSuchFieldException, SecurityException {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "list.listIntValue");
        final Type type = this.getClass().getField("listIntValueTypeToken").getGenericType();
        final Set<List<Integer>> values = this.sut.retrieve(null, type, map);

        Assert.assertTrue(values.size() == 1);
        Assert.assertTrue(values.contains(Arrays.asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void mapStringIntValue() throws NoSuchFieldException, SecurityException {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "map.mapStringIntValue");
        final Type type = this.getClass().getField("mapStringIntValueTypeToken").getGenericType();
        final Set<Map<String, Integer>> values = this.sut.retrieve(null, type, map);

        Assert.assertTrue(values.size() == 1);
        final Map<String, Integer> expected = new HashMap<>();
        expected.put("a", 1);
        expected.put("b", 2);
        expected.put("c", 3);
        Assert.assertTrue(values.contains(expected));
    }

    @Test
    public void mapIntStringValue() throws NoSuchFieldException, SecurityException {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "map.mapIntStringValue");
        final Type type = this.getClass().getField("mapIntStringValueTypeToken").getGenericType();
        final Set<Map<String, Integer>> values = this.sut.retrieve(null, type, map);

        Assert.assertTrue(values.size() == 1);
        final Map<Integer, String> expected = new HashMap<>();
        expected.put(1, "a");
        expected.put(2, "b");
        expected.put(3, "c");
        Assert.assertTrue(values.contains(expected));
    }

    @Test
    public void mapColourStringValue() throws NoSuchFieldException, SecurityException {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "map.mapColourStringValue");
        final Type type = this.getClass().getField("mapColourStringValueTypeToken").getGenericType();
        final Set<Map<Colour, Integer>> values = this.sut.retrieve(null, type, map);

        Assert.assertTrue(values.size() == 1);
        final Map<Colour, String> expected = new HashMap<>();
        expected.put(Colour.RED, "red");
        expected.put(Colour.GREEN, "green");
        expected.put(Colour.BLUE, "blue");
        Assert.assertTrue(values.contains(expected));
    }

    @Test
    public void mapStringPersonValue() throws NoSuchFieldException, SecurityException {
        final Map<String, Object> map = new HashMap<>();
        map.put("path", "map.mapStringPersonValue");
        final Type type = this.getClass().getField("mapStringPersonValueTypeToken").getGenericType();
        final Set<Map<String, Person>> values = this.sut.retrieve(null, type, map);

        Assert.assertTrue(values.size() == 1);
        final Map<String, Person> expected = new HashMap<>();
        expected.put("Fred", new Person("Fred", 13, Colour.BLUE));
        expected.put("Bob", new Person("Bob", 18, Colour.GREEN));
        expected.put("Jane", new Person("Jane", 23, Colour.RED));
        Assert.assertTrue(values.contains(expected));
    }
}
