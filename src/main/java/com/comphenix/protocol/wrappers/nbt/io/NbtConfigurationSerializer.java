package com.comphenix.protocol.wrappers.nbt.io;

import com.comphenix.protocol.wrappers.nbt.NbtBase;
import com.comphenix.protocol.wrappers.nbt.NbtCompound;
import com.comphenix.protocol.wrappers.nbt.NbtFactory;
import com.comphenix.protocol.wrappers.nbt.NbtList;
import com.comphenix.protocol.wrappers.nbt.NbtType;
import com.comphenix.protocol.wrappers.nbt.NbtWrapper;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** ConfigurationSection adapter preserving NBT types in encoded keys. */
public class NbtConfigurationSerializer {
    public static final String TYPE_DELIMITER = "$";
    public static final NbtConfigurationSerializer DEFAULT = new NbtConfigurationSerializer();

    private final String dataTypeDelimiter;

    public NbtConfigurationSerializer() {
        this(TYPE_DELIMITER);
    }

    public NbtConfigurationSerializer(String dataTypeDelimiter) {
        this.dataTypeDelimiter = dataTypeDelimiter == null ? TYPE_DELIMITER : dataTypeDelimiter;
    }

    public String getDataTypeDelimiter() {
        return dataTypeDelimiter;
    }

    public <TType> void serialize(NbtBase<TType> value, ConfigurationSection destination) {
        if (value == null || destination == null) {
            throw new IllegalArgumentException("value and destination are required");
        }
        write(value, destination, value.getName());
    }

    public <TType> NbtWrapper<TType> deserialize(ConfigurationSection root, String nodeName) {
        return (NbtWrapper<TType>) read(root, nodeName);
    }

    public NbtCompound deserializeCompound(YamlConfiguration root, String nodeName) {
        return (NbtCompound) read(root, nodeName);
    }

    public <T> NbtList<T> deserializeList(YamlConfiguration root, String nodeName) {
        return (NbtList<T>) read(root, nodeName);
    }

    public Object toNodeValue(Object value, NbtType type) {
        if (type == NbtType.TAG_INT_ARRAY && value instanceof byte[]) {
            return toIntegerArray((byte[]) value);
        }
        return value;
    }

    private void write(NbtBase<?> node, ConfigurationSection parent, String rawName) {
        String name = encode(rawName, node.getType());
        if (node instanceof NbtCompound) {
            ConfigurationSection section = parent.createSection(name);
            for (NbtBase<?> child : (NbtCompound) node) {
                write(child, section, child.getName());
            }
        } else if (node instanceof NbtList<?>) {
            NbtList<?> list = (NbtList<?>) node;
            if (list.getElementType().isComposite()) {
                ConfigurationSection section = parent.createSection(name);
                int index = 0;
                for (NbtBase<?> child : list.asCollection()) {
                    write(child, section, Integer.toString(index++));
                }
            } else {
                java.util.ArrayList<Object> values = new java.util.ArrayList<>();
                for (NbtBase<?> child : list.asCollection()) {
                    values.add(fromNodeValue(child));
                }
                parent.set(name, values);
            }
        } else {
            parent.set(name, fromNodeValue(node));
        }
    }

    private NbtBase<?> read(ConfigurationSection parent, String requestedName) {
        String actualName = findName(parent, requestedName);
        String[] decoded = decode(actualName);
        Object raw = parent.get(actualName);
        if (raw instanceof ConfigurationSection) {
            if (decoded.length > 1 && isListType(decoded[1])) {
                NbtList<Object> list = NbtFactory.ofList(decoded[0]);
                ConfigurationSection section = (ConfigurationSection) raw;
                java.util.List<String> keys = new java.util.ArrayList<>(section.getKeys(false));
                keys.sort((left, right) -> Integer.compare(Integer.parseInt(decode(left)[0]), Integer.parseInt(decode(right)[0])));
                for (String key : keys) {
                    NbtBase<?> child = read(section, key);
                    child.setName(NbtList.EMPTY_NAME);
                    list.add((NbtBase<Object>) child);
                }
                return list;
            }
            NbtCompound compound = NbtFactory.ofCompound(decoded[0]);
            for (String key : ((ConfigurationSection) raw).getKeys(false)) {
                compound.put(read((ConfigurationSection) raw, key));
            }
            return compound;
        }
        if (decoded.length < 2) {
            throw new IllegalArgumentException("Cannot find encoded NBT type for " + actualName);
        }
        NbtType type = NbtType.getTypeFromID(Integer.parseInt(decoded[1]));
        if (raw instanceof List<?>) {
            NbtList<Object> list = NbtFactory.ofList(decoded[0]);
            for (Object value : (List<?>) raw) {
                list.addClosest(toNodeValue(value, type), type);
            }
            return list;
        }
        return NbtFactory.ofWrapper(type, decoded[0], toNodeValue(raw, type));
    }

    private String findName(ConfigurationSection section, String requested) {
        if (section.contains(requested)) {
            return requested;
        }
        for (String key : section.getKeys(false)) {
            if (decode(key)[0].equals(requested)) {
                return key;
            }
        }
        throw new IllegalArgumentException("Unable to find node " + requested + " in " + section);
    }

    private String encode(String name, NbtType type) {
        return (name == null ? "" : name) + dataTypeDelimiter + type.getRawID();
    }

    private String[] decode(String name) {
        int index = name.lastIndexOf(dataTypeDelimiter);
        return index > 0 ? new String[]{name.substring(0, index), name.substring(index + dataTypeDelimiter.length())}
                : new String[]{name};
    }

    private boolean isListType(String rawType) {
        return NbtType.getTypeFromID(Integer.parseInt(rawType)) == NbtType.TAG_LIST;
    }

    private Object fromNodeValue(NbtBase<?> node) {
        if (node.getType() == NbtType.TAG_INT_ARRAY) {
            int[] values = (int[]) node.getValue();
            ByteBuffer buffer = ByteBuffer.allocate(values.length * Integer.BYTES);
            buffer.asIntBuffer().put(values);
            return buffer.array();
        }
        return node.getValue();
    }

    private static int[] toIntegerArray(byte[] data) {
        IntBuffer source = ByteBuffer.wrap(data).asIntBuffer();
        int[] result = new int[source.remaining()];
        source.get(result);
        return result;
    }
}
