package org.eclipse.ditto.client.internal.bus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.eclipse.ditto.json.JsonPointer;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Unit test for {@link org.eclipse.ditto.client.internal.bus.DefaultRegistry}.
 */
public final class DefaultRegistryTest {

    private final DefaultRegistry<Consumer> registry = new DefaultRegistry<>();

    @Test
    public void verifyMultipleConsumersOnSameSelector() {
        final JsonPointer pointer = JsonPointer.of("/aThing");
        final Consumer consumerA = Mockito.mock(Consumer.class);
        final Consumer consumerB = Mockito.mock(Consumer.class);
        final DefaultJsonPointerSelector selectorA = DefaultJsonPointerSelector.jsonPointerSelector(pointer);
        final DefaultJsonPointerSelector selectorB = DefaultJsonPointerSelector.jsonPointerSelector(pointer);


        final Registration<Consumer> registrationA = registry.register(selectorA, consumerA);
        selectionShouldContain(registry.select(pointer), consumerA);

        registry.register(selectorB, consumerB);

        selectionShouldContain(registry.select(pointer), consumerA, consumerB);

        registrationA.cancel();

        selectionShouldContain(registry.select(pointer), consumerB);
    }

    @SafeVarargs
    private final <T> void selectionShouldContain(final List<Registration<T>> selection,
            final T... objects) {
        assertThat(selection).hasSize(objects.length);
        assertThat(selection.stream().map(Registration::getRegisteredObject)).contains(objects);
    }

}
