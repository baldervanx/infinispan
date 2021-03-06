package org.infinispan.util.function;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * This is a functional interface that is the same as a {@link Consumer} except that it must also be
 * {@link Serializable}
 *
 * @author wburns
 * @since 9.0
 */
public interface SerializableConsumer<T> extends Serializable, Consumer<T> {
}
