/*
 * Copyright (c) 2016-present, RxJava Contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */
package se.chalmers.ju2jmh.api;

import org.junit.rules.MethodRule;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

/**
 * @author massi
 *
 */
public abstract class JU2JmhBenchmark {
    @FunctionalInterface
    public interface ThrowingRunnable {
        /**
         * @throws Throwable
         */
        void run() throws Throwable;
    }

    /**
     * @throws Throwable
     */
    public abstract void createImplementation() throws Throwable;

    /**
     * @return something
     */
    public abstract Object implementation();

    /**
     * @throws Throwable
     */
    public void beforeClass() throws Throwable {}
    
    /**
     * @throws Throwable
     */
    public void afterClass() throws Throwable {}
    
    /**
     * @throws Throwable
     */
    public void before() throws Throwable {}
    
    /**
     * @throws Throwable
     */
    public void after() throws Throwable {}

    /**
     * @param statement
     * @param description
     * @return something
     */
    public Statement applyClassRuleFields(Statement statement, Description description) {
        return statement;
    }

    /**
     * @param statement
     * @param description
     * @return something
     */
    public Statement applyClassRuleMethods(Statement statement, Description description) {
        return statement;
    }

    /**
     * @param statement
     * @param description
     * @return something
     */
    public Statement applyRuleFields(Statement statement, Description description) {
        return statement;
    }

    /**
     * @param statement
     * @param description
     * @return something
     */
    public Statement applyRuleMethods(Statement statement, Description description) {
        return statement;
    }

    /**
     * @param methodName
     * @return something
     */
    public final Description description(String methodName) {
        return Description.createTestDescription(implementation().getClass(), methodName);
    }

    /**
     * @param description
     * @return something
     */
    private FrameworkMethod frameworkMethodFromDescription(Description description) {
        FrameworkMethod frameworkMethod;
        try {
            frameworkMethod = new FrameworkMethod(
                    implementation().getClass().getMethod(description.getMethodName()));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        return frameworkMethod;
    }

    /**
     * @param rule
     * @param statement
     * @param description
     * @return something
     */
    public final Statement applyRule(TestRule rule, Statement statement, Description description) {
        if (rule.getClass() == Timeout.class) {
            return statement;
        }
        return rule.apply(statement, description);
    }

    /**
     * @param rule
     * @param statement
     * @param description
     * @return something
     */
    public final Statement applyRule(MethodRule rule, Statement statement,
            Description description) {
        return rule.apply(statement, frameworkMethodFromDescription(description), implementation());
    }

    /**
     * @author massi
     *
     */
    private static class BeforeAfterStatement extends Statement {
        private final ThrowingRunnable beforeAction;
        private final ThrowingRunnable action;
        private final ThrowingRunnable afterAction;

        /**
         * @param beforeAction
         * @param action
         * @param afterAction
         */
        private BeforeAfterStatement(
                ThrowingRunnable beforeAction, ThrowingRunnable action,
                ThrowingRunnable afterAction) {
            this.beforeAction = beforeAction;
            this.action = action;
            this.afterAction = afterAction;
        }

        /**
         *
         */
        @Override
        public void evaluate() throws Throwable {
            beforeAction.run();
            try {
                action.run();
            } finally {
                afterAction.run();
            }
        }
    }

    /**
     * @param benchmark
     * @param description
     * @throws Throwable
     */
    public final void runBenchmark(ThrowingRunnable benchmark, Description description)
            throws Throwable {
        Statement statement = new BeforeAfterStatement(this::before, benchmark, this::after);
        statement = applyRuleMethods(statement, description);
        statement = applyRuleFields(statement, description);
        statement = new BeforeAfterStatement(
                this::beforeClass, statement::evaluate, this::afterClass);
        statement = applyClassRuleMethods(statement, description);
        statement = applyClassRuleFields(statement, description);
        statement.evaluate();
    }

    /**
     * @param benchmark
     * @param description
     * @param expected
     * @throws Throwable
     */
    public final void runExceptionBenchmark(ThrowingRunnable benchmark, Description description,
                                            Class<? extends Throwable> expected) throws Throwable {
        ThrowingRunnable exceptionBenchmark = () -> {
            try {
                benchmark.run();
            } catch (Throwable e) {
                if (expected.isInstance(e)) {
                    return;
                }
                throw e;
            }
            throw new AssertionError(
                    "Expected " + expected.getCanonicalName() + " but none was thrown");
        };
        runBenchmark(exceptionBenchmark, description);
    }
}