package net.okocraft.box.plugin.command;

import com.google.common.collect.ForwardingList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A class that wraps array arguments and list arguments.
 *
 * <b>The methods that modify this instance, such as {@link List#add(Object)} and {@link List#remove(Object)},
 * behave differently depending on the implementation of the list used when creating the instance.
 * Therefore, methods that modify the list should not be called.</b>
 *
 * @since 1.0
 */
public class ArgumentList extends ForwardingList<String> {

    private final List<String> args;

    /**
     * Creates an ArgumentList with array type arguments.
     *
     * @param args an array type arguments
     */
    public ArgumentList(@NotNull String[] args) {
        this(Arrays.asList(args));
    }

    /**
     * Creates an ArgumentList with list type arguments.
     *
     * @param args a list type arguments
     */
    public ArgumentList(@NotNull List<String> args) {
        Objects.requireNonNull(args);

        this.args = args;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    protected List<String> delegate() {
        return args;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public ArgumentList subList(int fromIndex, int toIndex) {
        return new ArgumentList(super.subList(fromIndex, toIndex));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o instanceof ArgumentList) {
            ArgumentList that = (ArgumentList) o;
            return args.equals(that.args);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(args);
    }

    @Override
    public String toString() {
        return "ArgumentList{" +
                "args=" + args +
                '}';
    }

    /**
     * Returns the element at the specified position in this list as a string.
     * <p>
     * If the specified position is out of the list, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     */
    @NotNull
    public String getOrDefault(int index, @NotNull String def) {
        if (hasElement(index)) {
            return get(index);
        } else {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a string.
     * <p>
     * If the specified position is out of the list, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @since 1.2
     */
    @Nullable
    public String getOrNull(int index) {
        if (hasElement(index)) {
            return get(index);
        } else {
            return null;
        }
    }

    /**
     * Returns the element at the specified position in this list as a boolean.
     * <p>
     * If it cannot be converted to double, it returns false.
     * <p>
     * This method is the same as the following code:
     * {@code getBooleanOrDefault(index, false)}
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list or false
     */
    public boolean getBoolean(int index) {
        return getBooleanOrDefault(index, false);
    }

    /**
     * Returns the element at the specified position in this list as a boolean.
     * <p>
     * If the specified position is out of the list, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     * @see Boolean#parseBoolean(String)
     */
    public boolean getBooleanOrDefault(int index, boolean def) {
        if (hasElement(index)) {
            return Boolean.parseBoolean(get(index));
        } else {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a boolean.
     * <p>
     * If the specified position is out of the list, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @see Boolean#parseBoolean(String)
     * @since 1.2
     */
    @Nullable
    public Boolean getBooleanOrNull(int index) {
        if (!hasElement(index)) {
            return null;
        }

        switch (get(index).toLowerCase()) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                return null;
        }
    }

    /**
     * Returns the element at the specified position in this list as a int.
     * <p>
     * If it cannot be converted to int, it returns 0.
     * <p>
     * This method is the same as the following code:
     * {@code getIntOrDefault(index, 0)}
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list or 0
     */
    public int getInt(int index) {
        return getIntOrDefault(index, 0);
    }

    /**
     * Returns the element at the specified position in this list as a integer.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a integer, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     */
    public int getIntOrDefault(int index, int def) {
        if (!hasElement(index)) {
            return def;
        }

        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a integer.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a integer, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @since 1.2
     */
    @Nullable
    public Integer getIntOrNull(int index) {
        if (!hasElement(index)) {
            return null;
        }

        try {
            return Integer.parseInt(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }


    /**
     * Returns the element at the specified position in this list as a long.
     * <p>
     * If it cannot be converted to long, it returns 0.
     * <p>
     * This method is the same as the following code:
     * {@code getLongOrDefault(index, 0)}
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list or 0
     */
    public long getLong(int index) {
        return getLongOrDefault(index, 0);
    }

    /**
     * Returns the element at the specified position in this list as a long.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a long, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     */
    public long getLongOrDefault(int index, long def) {
        if (!hasElement(index)) {
            return def;
        }

        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a long.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a long, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @since 1.2
     */
    @Nullable
    public Long getLongOrNull(int index) {
        if (!hasElement(index)) {
            return null;
        }

        try {
            return Long.parseLong(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns the element at the specified position in this list as a float.
     * <p>
     * If it cannot be converted to float, it returns 0.
     * <p>
     * This method is the same as the following code:
     * {@code getFloatOrDefault(index, 0.0f)}
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list or 0.0
     */
    public float getFloat(int index) {
        return getFloatOrDefault(index, 0.0f);
    }

    /**
     * Returns the element at the specified position in this list as a float.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a float, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     */
    public float getFloatOrDefault(int index, float def) {
        if (!hasElement(index)) {
            return def;
        }

        try {
            return Float.parseFloat(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a float.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a float, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @since 1.2
     */
    @Nullable
    public Float getFloatOrNull(int index) {
        if (!hasElement(index)) {
            return null;
        }

        try {
            return Float.parseFloat(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Returns the element at the specified position in this list as a double.
     * <p>
     * If it cannot be converted to double, it returns 0.
     * <p>
     * This method is the same as the following code:
     * {@code getDoubleOrDefault(index, 0.0)}
     *
     * @param index index of the element to return.
     * @return the element at the specified position in this list or 0.0
     */
    public double getDouble(int index) {
        return getDoubleOrDefault(index, 0.0);
    }

    /**
     * Returns the element at the specified position in this list as a double.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a double, the default value is returned.
     *
     * @param index index of the element to return.
     * @param def   the default value
     * @return the element at the specified position in this list or the default value
     */
    public double getDoubleOrDefault(int index, double def) {
        if (!hasElement(index)) {
            return def;
        }

        try {
            return Double.parseDouble(get(index));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Returns the element at the specified position in this list as a double.
     * <p>
     * If the specified position is out of the list or the element string could not be converted to a double, {@code null} is returned.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list or {@code null}
     * @since 1.2
     */
    @Nullable
    public Double getDoubleOrNull(int index) {
        if (!hasElement(index)) {
            return null;
        }

        try {
            return Double.parseDouble(get(index));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Gets the player whose name is the string at the specified position.
     * <p>
     * Returns null if the player with that name is not currently online.
     *
     * @param index index of the element to search for player.
     * @return the instance if the player is found, null if not found.
     * @see Bukkit#getPlayer(String)
     */
    @Nullable
    public Player getPlayer(int index) {
        return hasElement(index) ? Bukkit.getPlayer(get(index)) : null;
    }

    /**
     * Gets the player whose name is the string at the specified position.
     * <p>
     * Returns null if the player with that name is not currently online.
     *
     * @param index index of the element to search for player.
     * @return the instance if the player is found, null if not found.
     * @see Bukkit#getPlayerExact(String)
     */
    @Nullable
    public Player getPlayerExact(int index) {
        return hasElement(index) ? Bukkit.getPlayerExact(get(index)) : null;
    }

    /**
     * Checks that this list has elements in the specified position.
     * <p>
     * This method tests on {@literal -1 < index} and {@literal index <} {@link List#size()}.
     *
     * @param index position to check
     * @return true if the element is in the specified position in the list, false otherwise
     */
    public boolean hasElement(int index) {
        return -1 < index && index < args.size();
    }

    /**
     * Copy and return the argument list.
     * <p>
     * The list returned by this method is immutable.
     *
     * @return the argument list
     */
    @NotNull
    @Unmodifiable
    public List<String> getArguments() {
        return Collections.unmodifiableList(args);
    }
}