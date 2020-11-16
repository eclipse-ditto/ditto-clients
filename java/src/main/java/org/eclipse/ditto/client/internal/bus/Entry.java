package org.eclipse.ditto.client.internal.bus;

/**
 * Similar to Map.Entry but with object reference identity and fixed key type to act as identifier for
 * a subscription.
 */
final class Entry<T> implements AdaptableBus.SubscriptionId {

    private final Classification key;
    private final T value;

    public Entry(final Classification key, final T value) {
        this.key = key;
        this.value = value;
    }

    public Classification getKey() {
        return key;
    }

    public T getValue() {
        return value;
    }
}
