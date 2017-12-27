package com.aurea.apriori

import de.mrapp.apriori.Item
import org.jetbrains.annotations.NotNull

import static de.mrapp.util.Condition.ensureNotEmpty
import static de.mrapp.util.Condition.ensureNotNull

/**
 * An implementation of the type {@link de.mrapp.apriori.Item}, which is used for test purposes. Each item can
 * unambiguously be identified via its name.
 *
 * @author Michael Rapp
 */
class NamedItem implements Item {

    /**
     * The constant serial version UID.
     */
    private static final long serialVersionUID = 1L

    /**
     * The name of the item.
     */
    private final String name

    /**
     * Creates a new implementation of the type {@link Item}.
     *
     * @param name The name of the item as a {@link String}. The name may neither be null, nor
     *             empty
     */
    NamedItem(@NotNull final String name) {
        ensureNotNull(name, "The name may not be null")
        ensureNotEmpty(name, "The name may not be empty")
        this.name = name
    }

    /**
     * Returns the name of the item.
     *
     * @return The name of the item as a {@link String}. The name may neither be null, nor empty
     */
    @NotNull
    final String getName() {
        name
    }

    @Override
    final int compareTo(@NotNull final Item o) {
        toString() <=> o.toString()
    }

    @Override
    final String toString() {
        getName()
    }


    @Override
    final int hashCode() {
        final int prime = 31
        int result = 1
        result = prime * result + name.hashCode()
        result
    }

    @Override
    final boolean equals(final Object obj) {
        if (this == obj)
            return true
        if (obj == null)
            return false
        if (getClass() != obj.getClass())
            return false
        name == (obj as NamedItem).name
    }
}