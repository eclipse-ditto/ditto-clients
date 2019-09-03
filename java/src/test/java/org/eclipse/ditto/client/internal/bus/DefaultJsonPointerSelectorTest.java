package org.eclipse.ditto.client.internal.bus;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit test for {@link org.eclipse.ditto.client.internal.bus.DefaultJsonPointerSelector}.
 */
public final class DefaultJsonPointerSelectorTest {

    /**
     * This test shall verify that DefaultJsonPointerSelector does not implement equals and hashCode, since it would
     * break functionality of {@link DefaultRegistry} at the current state.
     */
    @Test
    public void verifyInequality() {
        final DefaultJsonPointerSelector p1 = DefaultJsonPointerSelector.jsonPointerSelector("/any/things/attributes");
        final DefaultJsonPointerSelector p2 = DefaultJsonPointerSelector.jsonPointerSelector("/any/things/attributes");
        assertThat(p1).isNotEqualTo(p2);
    }

}
