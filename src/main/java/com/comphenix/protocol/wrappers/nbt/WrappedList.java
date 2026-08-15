package com.comphenix.protocol.wrappers.nbt;

import com.comphenix.protocol.wrappers.nbt.io.NbtBinarySerializer;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/** In-memory list implementation used by the clean-room NBT backend. */
class WrappedList<TType> implements NbtWrapper<List<NbtBase<TType>>>, NbtList<TType> {
    private String name;
    private final List<NbtBase<TType>> values = new ArrayList<>();
    private NbtType elementType = NbtType.TAG_END;
    private final Object nativeHandle;

    public static <T> NbtList<T> fromName(String name) {
        return new WrappedList<>(name);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> NbtList<T> fromArray(String name, T... elements) {
        WrappedList<T> result = new WrappedList<>(name);
        if (elements != null) {
            for (T element : elements) {
                if (element instanceof NbtBase) {
                    result.add((NbtBase) element);
                } else {
                    result.addClosest(element, NbtType.getTypeFromClass(element.getClass()));
                }
            }
        }
        return result;
    }

    public static <T> NbtList<T> fromList(String name, Collection<? extends T> elements) {
        WrappedList<T> result = new WrappedList<>(name);
        if (elements != null) {
            for (T element : elements) {
                if (element instanceof NbtBase<?>) {
                    @SuppressWarnings("unchecked")
                    NbtBase<T> base = (NbtBase<T>) element;
                    result.add(base);
                } else {
                    result.addClosest(element, NbtType.getTypeFromClass(element.getClass()));
                }
            }
        }
        return result;
    }

    private WrappedList(String name) {
        this.name = name == null ? "" : name;
        this.nativeHandle = null;
    }

    public WrappedList(Object handle) {
        this(handle, "");
    }

    @SuppressWarnings("unchecked")
    public WrappedList(Object handle, String name) {
        this.name = name == null ? "" : name;
        this.nativeHandle = handle instanceof NbtList<?> ? ((NbtList<?>) handle).getHandle() : handle;
        if (handle instanceof NbtList<?>) {
            for (Object value : (NbtList<?>) handle) {
                addClosest(value, NbtType.getTypeFromClass(value.getClass()));
            }
        } else if (handle instanceof List<?>) {
            for (Object value : (List<?>) handle) {
                if (value instanceof NbtBase<?>) {
                    add((NbtBase<TType>) value);
                } else if (value != null) {
                    addClosest(value, NbtType.getTypeFromClass(value.getClass()));
                }
            }
        }
    }

    @Override
    public boolean accept(NbtVisitor visitor) {
        if (visitor.visitEnter(this)) {
            for (NbtBase<TType> entry : values) {
                if (!entry.accept(visitor)) {
                    break;
                }
            }
        }
        return visitor.visitLeave(this);
    }

    @Override
    public Object getHandle() {
        return nativeHandle == null ? this : nativeHandle;
    }

    @Override
    public NbtType getType() {
        return NbtType.TAG_LIST;
    }

    @Override
    public NbtType getElementType() {
        if (values.isEmpty()) {
            return NbtType.TAG_END;
        }
        NbtType first = values.get(0).getType();
        for (NbtBase<TType> entry : values) {
            if (entry.getType() != first) {
                return NbtType.TAG_COMPOUND;
            }
        }
        return first;
    }

    @Override
    public void setElementType(NbtType type) {
        if (type == null || type == NbtType.TAG_END) {
            elementType = NbtType.TAG_END;
        } else {
            elementType = type;
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name == null ? "" : name;
    }

    @Override
    public List<NbtBase<TType>> getValue() {
        return values;
    }

    @Override
    public void setValue(List<NbtBase<TType>> newValue) {
        values.clear();
        if (newValue != null) {
            for (NbtBase<TType> value : newValue) {
                add(value);
            }
        }
    }

    @Override
    public void addClosest(Object value) {
        if (elementType == NbtType.TAG_END) {
            throw new IllegalArgumentException("Set an element type before calling addClosest(Object)");
        }
        addClosest(value, elementType);
    }

    @Override
    public void addClosest(Object value, NbtType type) {
        if (value == null || type == null || type == NbtType.TAG_END) {
            throw new IllegalArgumentException("A list element and type are required");
        }
        Object converted = convertValue(value, type);
        @SuppressWarnings("unchecked")
        NbtBase<TType> entry = (NbtBase<TType>) NbtFactory.ofWrapper(type, EMPTY_NAME, converted);
        add(entry);
    }

    @Override
    public void add(NbtBase<TType> element) {
        if (element == null) {
            throw new IllegalArgumentException("Cannot store NULL elements in list.");
        }
        if (!EMPTY_NAME.equals(element.getName())) {
            throw new IllegalArgumentException("Cannot add named NBT tags to a list.");
        }
        if (elementType != NbtType.TAG_END && element.getType() != elementType) {
            throw new IllegalArgumentException("Cannot add " + element.getType() + " to a list of " + elementType);
        }
        @SuppressWarnings("unchecked")
        NbtBase<TType> copy = (NbtBase<TType>) element.deepClone();
        copy.setName(EMPTY_NAME);
        values.add(copy);
        if (elementType == NbtType.TAG_END) {
            elementType = element.getType();
        }
    }

    @Override
    public void add(String value) {
        addClosest(value, NbtType.TAG_STRING);
    }

    @Override
    public void add(byte value) {
        addClosest(value, NbtType.TAG_BYTE);
    }

    @Override
    public void add(short value) {
        addClosest(value, NbtType.TAG_SHORT);
    }

    @Override
    public void add(int value) {
        addClosest(value, NbtType.TAG_INT);
    }

    @Override
    public void add(long value) {
        addClosest(value, NbtType.TAG_LONG);
    }

    @Override
    public void add(double value) {
        addClosest(value, NbtType.TAG_DOUBLE);
    }

    @Override
    public void add(byte[] value) {
        addClosest(value, NbtType.TAG_BYTE_ARRAY);
    }

    @Override
    public void add(int[] value) {
        addClosest(value, NbtType.TAG_INT_ARRAY);
    }

    @Override
    public void remove(Object remove) {
        if (remove instanceof NbtBase<?>) {
            values.remove(remove);
            return;
        }
        values.removeIf(entry -> java.util.Objects.equals(entry.getValue(), remove));
    }

    @Override
    public TType getValue(int index) {
        return values.get(index).getValue();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Collection<NbtBase<TType>> asCollection() {
        return values;
    }

    @Override
    public Iterator<TType> iterator() {
        Iterator<NbtBase<TType>> delegate = values.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return delegate.hasNext();
            }

            @Override
            public TType next() {
                return delegate.next().getValue();
            }

            @Override
            public void remove() {
                delegate.remove();
            }
        };
    }

    @Override
    public NbtList<TType> deepClone() {
        WrappedList<TType> clone = new WrappedList<>(name);
        clone.elementType = elementType;
        for (NbtBase<TType> value : values) {
            clone.add(value);
        }
        return clone;
    }

    @Override
    public void write(DataOutput destination) {
        NbtBinarySerializer.DEFAULT.serialize(this, destination);
    }

    private static Object convertValue(Object value, NbtType type) {
        if (!(value instanceof Number)) {
            return value;
        }
        Number number = (Number) value;
        return switch (type) {
            case TAG_BYTE -> number.byteValue();
            case TAG_SHORT -> number.shortValue();
            case TAG_INT -> number.intValue();
            case TAG_LONG -> number.longValue();
            case TAG_FLOAT -> number.floatValue();
            case TAG_DOUBLE -> number.doubleValue();
            default -> value;
        };
    }

    @Override
    public String toString() {
        return values.toString();
    }

    @Override public int hashCode() { return java.util.Objects.hash(name, values); }
    @Override public boolean equals(Object other) {
        if (!(other instanceof NbtList<?> list)) return false;
        return java.util.Objects.equals(name, list.getName())
                && java.util.Objects.equals(values, list.getValue());
    }
}
