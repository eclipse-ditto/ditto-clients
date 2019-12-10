package org.eclipse.ditto.client.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.AbstractAssert;
import org.eclipse.ditto.model.policies.Policy;
import org.eclipse.ditto.model.policies.PolicyId;
import org.eclipse.ditto.signals.commands.policies.PolicyCommand;

/**
 * An assert for {@link Policy}.
 */
public final class PolicyAssert extends AbstractAssert<PolicyAssert, PolicyCommand> {

    /**
     * Creates a new instance of {@code PolicyAssert}.
     *
     * @param actual the adaptable to be checked.
     */
    public PolicyAssert(final PolicyCommand actual) {
        super(actual, PolicyAssert.class);
    }

    public PolicyAssert hasPolicyId(final PolicyId expectedPolicyId) {
        return assertThatEqual(expectedPolicyId, actual.getEntityId(), "Policy identifier");
    }

    private <T> PolicyAssert assertThatEqual(final T expected, final T actual, final String propertyName) {
        isNotNull();
        assertThat(actual).overridingErrorMessage("Expected command to have %s \n<%s> but it had \n<%s>", propertyName,
                expected,
                actual).isEqualTo(expected);
        return this;
    }

    public PolicyAssert hasType(final String expectedType) {
        return assertThatEqual(expectedType, actual.getType(), "type");
    }
}
