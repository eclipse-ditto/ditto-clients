/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.client.twin.internal;

import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.eclipse.ditto.json.JsonKey;
import org.eclipse.ditto.json.JsonPointer;
import org.eclipse.ditto.model.query.criteria.Predicate;
import org.eclipse.ditto.model.query.criteria.visitors.CriteriaVisitor;
import org.eclipse.ditto.model.query.criteria.visitors.PredicateVisitor;
import org.eclipse.ditto.model.query.expression.ExistsFieldExpression;
import org.eclipse.ditto.model.query.expression.FilterFieldExpression;
import org.eclipse.ditto.model.query.expression.visitors.FieldExpressionVisitor;
import org.eclipse.ditto.model.things.Feature;
import org.eclipse.ditto.model.things.Thing;

final class ToStringVisitors {

    static final CriteriaVisitor<String> CRITERIA = new CriteriaToStringVisitor();

    static final FieldExpressionVisitor<String> FIELD_EXPRESSION = new FieldExpressionToStringVisitor();

    static final PredicateVisitor<UnaryOperator<String>> PREDICATE = new PredicateToStringVisitor();

    private static final class CriteriaToStringVisitor implements CriteriaVisitor<String> {

        @Override
        public String visitAnd(final List<String> conjuncts) {
            return conjuncts.size() == 1
                    ? conjuncts.get(0)
                    : conjuncts.stream().collect(Collectors.joining(",", "and(", ")"));
        }

        @Override
        public String visitAny() {
            return "exists(thingId)";
        }

        @Override
        public String visitExists(final ExistsFieldExpression fieldExpression) {
            return "exists(" + fieldExpression.accept(FIELD_EXPRESSION) + ")";
        }

        @Override
        public String visitField(final FilterFieldExpression fieldExpression, final Predicate predicate) {
            return predicate.accept(PREDICATE).apply(fieldExpression.accept(FIELD_EXPRESSION));
        }

        @Override
        public String visitNor(final List<String> negativeDisjoints) {
            return negativeDisjoints.size() == 1
                    ? "not(" + negativeDisjoints.get(0) + ")"
                    : negativeDisjoints.stream().collect(Collectors.joining(",", "not(or(", "))"));
        }

        @Override
        public String visitOr(final List<String> disjoints) {
            return disjoints.size() == 1
                    ? disjoints.get(0)
                    : disjoints.stream().collect(Collectors.joining(",", "or(", ")"));
        }
    }

    private static final class FieldExpressionToStringVisitor implements FieldExpressionVisitor<String> {

        private FieldExpressionToStringVisitor() {}

        @Override
        public String visitAttribute(final String key) {
            return Thing.JsonFields.ATTRIBUTES.getPointer().append(JsonPointer.of(key)).toString();
        }

        @Override
        public String visitFeature(final String featureId) {
            return Thing.JsonFields.FEATURES.getPointer().addLeaf(JsonKey.of(featureId)).toString();
        }

        @Override
        public String visitFeatureIdProperty(final String featureId, final String property) {
            return Thing.JsonFields.FEATURES.getPointer()
                    .addLeaf(JsonKey.of(featureId))
                    .append(Feature.JsonFields.PROPERTIES.getPointer())
                    .append(JsonPointer.of(property))
                    .toString();
        }

        @Override
        public String visitSimple(final String fieldName) {
            return fieldName;
        }
    }

    private static final class PredicateToStringVisitor implements PredicateVisitor<UnaryOperator<String>> {

        private PredicateToStringVisitor() {}

        @Override
        public UnaryOperator<String> visitEq(final Object value) {
            return op("eq", value);
        }

        @Override
        public UnaryOperator<String> visitGe(final Object value) {
            return op("ge", value);
        }

        @Override
        public UnaryOperator<String> visitGt(final Object value) {
            return op("gt", value);
        }

        @Override
        public UnaryOperator<String> visitIn(final List<?> values) {
            final String arguments = values.stream()
                    .map(PredicateToStringVisitor::quote)
                    .collect(Collectors.joining(","));
            return opUnquoted("in", arguments);
        }

        @Override
        public UnaryOperator<String> visitLe(final Object value) {
            return op("le", value);
        }

        @Override
        public UnaryOperator<String> visitLike(final String value) {
            return op("like", value);
        }

        @Override
        public UnaryOperator<String> visitLt(final Object value) {
            return op("lt", value);
        }

        @Override
        public UnaryOperator<String> visitNe(final Object value) {
            return op("ne", value);
        }

        private static UnaryOperator<String> op(final String opName, final Object value) {
            return opUnquoted(opName, quote(value));
        }

        private static UnaryOperator<String> opUnquoted(final String opName, final String argument) {
            return s -> opName + "(" + s + "," + argument + ")";
        }

        private static String quote(final Object value) {
            return value instanceof CharSequence
                    ? "\"" + value.toString().replace("\"", "\\\"") + "\""
                    : Objects.toString(value);
        }
    }
}
