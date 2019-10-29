/*
 * Copyright (c) 2014, Francis Galiegue (fgaliegue@gmail.com)
 *
 * This software is dual-licensed under:
 *
 * - the Lesser General Public License (LGPL) version 3.0 or, at your option, any
 *   later version;
 * - the Apache Software License (ASL) version 2.0.
 *
 * The text of this file and of both licenses is available at the root of this
 * project or, if you have the jar distribution, in directory META-INF/, under
 * the names LGPL-3.0.txt and ASL-2.0.txt respectively.
 *
 * Direct link to the sources:
 *
 * - LGPL 3.0: https://www.gnu.org/licenses/lgpl-3.0.txt
 * - ASL 2.0: http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package com.github.fge.jsonschema.core.keyword.syntax.checkers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.fge.jackson.JacksonUtils;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jackson.NodeType;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonschema.SampleNodeProvider;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.tree.key.SchemaKey;
import com.github.fge.jsonschema.core.util.Dictionary;
import com.github.fge.jsonschema.core.messages.JsonSchemaSyntaxMessageBundle;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.github.fge.jsonschema.core.report.ProcessingReport;
import com.github.fge.jsonschema.core.tree.CanonicalSchemaTree;
import com.github.fge.jsonschema.core.tree.SchemaTree;
import com.github.fge.msgsimple.bundle.MessageBundle;
import com.github.fge.msgsimple.load.MessageBundles;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import static com.github.fge.jsonschema.TestUtils.*;
import static com.github.fge.jsonschema.matchers.ProcessingMessageAssert.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test
public abstract class SyntaxCheckersTest
{
    private static final MessageBundle BUNDLE
        = MessageBundles.getBundle(JsonSchemaSyntaxMessageBundle.class);

    /*
     * The keyword
     */
    private final String keyword;
    /*
     * The syntax checker
     */
    private final SyntaxChecker checker;
    /*
     * The set of invalid types for that keyword
     */
    private final EnumSet<NodeType> invalidTypes;
    /*
     * The value test node, if any
     */
    private final JsonNode valueTests;
    /*
     * The pointer test node, if any
     */
    private final JsonNode pointerTests;

    /*
     * Per test variables
     */
    private List<JsonPointer> pointers;
    private ProcessingReport report;

    /**
     * Constructor
     *
     * @param dict the {@link Dictionary} of {@link SyntaxChecker}s
     * @param prefix the prefix to use for resource files
     * @param keyword the keyword to test
     * @throws JsonProcessingException source JSON (if any) is not legal JSON
     */
    protected SyntaxCheckersTest(final Dictionary<SyntaxChecker> dict,
        final String prefix, final String keyword)
        throws JsonProcessingException
    {
        this.keyword = keyword;
        checker = dict.entries().get(keyword);
        invalidTypes = checker == null ? null
            : EnumSet.complementOf(checker.getValidTypes());
        /*
         * Try and load the data and affect pointers. Barf on invalid JSON.
         *
         * If IOException, it means no file (hopefully); affect a MissingNode
         * to both valueTests and pointerTests.
         */
        JsonNode valueTestsNode, pointerTestsNode;
        try {
            final String resource = "/syntax/" + prefix + '/' + keyword
                + ".json";
            final JsonNode data = JsonLoader.fromResource(resource);
            valueTestsNode = data.path("valueTests");
            pointerTestsNode = data.path("pointerTests");
        } catch (JsonProcessingException oops) {
            throw oops;
        } catch (IOException ignored) {
            valueTestsNode = MissingNode.getInstance();
            pointerTestsNode = MissingNode.getInstance();
        }

        valueTests = valueTestsNode;
        pointerTests = pointerTestsNode;
    }

    @BeforeMethod
    public final void init()
    {
        pointers = Lists.newArrayList();
        report = mock(ProcessingReport.class);
    }

    /*
     * First test: check the keyword's presence in the dictionary. All other
     * tests depend on this one.
     */
    @Test
    public final void keywordIsSupportedInThisDictionary()
    {
        assertNotNull(checker, "keyword " + keyword + " is not supported??");
    }

    /*
     * Second test: check that invalid values are reported as such. Test common
     * to all keywords.
     */
    @DataProvider
    public final Iterator<Object[]> invalidTypes()
    {
        return SampleNodeProvider.getSamples(invalidTypes);
    }

    @Test(
        dependsOnMethods = "keywordIsSupportedInThisDictionary",
        dataProvider = "invalidTypes"
    )
    public final void invalidTypesAreReportedAsErrors(final JsonNode node)
        throws ProcessingException
    {
        final SchemaTree tree = treeFromValue(keyword, node);
        final NodeType type = NodeType.getNodeType(node);
        final ArgumentCaptor<ProcessingMessage> captor
            = ArgumentCaptor.forClass(ProcessingMessage.class);

        checker.checkSyntax(pointers, BUNDLE, report, tree);

        verify(report).error(captor.capture());

        final ProcessingMessage msg = captor.getValue();
        final String message = BUNDLE.printf("common.incorrectType", type,
            EnumSet.complementOf(invalidTypes));
        assertMessage(msg).isSyntaxError(keyword, message, tree)
            .hasField("expected", EnumSet.complementOf(invalidTypes))
            .hasField("found", type);
    }

    /*
     * Third test: value tests. If no value tests were found, don't bother:
     * BasicSyntaxCheckerTest has covered that for us.
     */
    @DataProvider
    protected final Iterator<Object[]> getValueTests()
    {
        if (valueTests.isMissingNode())
            return ImmutableSet.<Object[]>of().iterator();

        final List<Object[]> list = Lists.newArrayList();

        String msg;
        JsonNode msgNode;
        JsonNode msgParams;
        JsonNode msgData;

        for (final JsonNode node: valueTests) {
            msgNode = node.get("message");
            msgParams = node.get("msgParams");
            msgData = node.get("msgData");
            msg = msgNode == null ? null
                : buildMessage(msgNode.textValue(), msgParams, msgData);
            list.add(new Object[]{ node.get("schema"), msg,
                node.get("valid").booleanValue(), msgData });
        }
        return list.iterator();
    }

    @Test(
        dependsOnMethods = "keywordIsSupportedInThisDictionary",
        dataProvider = "getValueTests"
    )
    public final void valueTestsSucceed(final JsonNode schema,
        final String msg, final boolean success, final ObjectNode msgData)
        throws ProcessingException
    {
        final SchemaTree tree
            = new CanonicalSchemaTree(SchemaKey.anonymousKey(), schema);

        checker.checkSyntax(pointers, BUNDLE, report, tree);

        if (success) {
            verify(report, never()).error(anyMessage());
            return;
        }

        final ArgumentCaptor<ProcessingMessage> captor
            = ArgumentCaptor.forClass(ProcessingMessage.class);
        verify(report).error(captor.capture());

        final ProcessingMessage message = captor.getValue();

        assertMessage(message).isSyntaxError(keyword, msg, tree)
            .hasContents(msgData);
    }

    /*
     * Fourth test: pointer lookups
     *
     * Non relevant keywrods will not have set it
     */
    @DataProvider
    protected final Iterator<Object[]> getPointerTests()
    {
        if (pointerTests.isMissingNode())
            return ImmutableSet.<Object[]>of().iterator();

        final List<Object[]> list = Lists.newArrayList();

        for (final JsonNode node: pointerTests)
            list.add(new Object[] {
                node.get("schema"), node.get("pointers")
            });

        return list.iterator();
    }

    @Test(
        dependsOnMethods = "keywordIsSupportedInThisDictionary",
        dataProvider = "getPointerTests"
    )
    public final void pointerDelegationWorksCorrectly(final JsonNode schema,
        final ArrayNode expectedPointers)
        throws ProcessingException, JsonPointerException
    {
        final SchemaTree tree
            = new CanonicalSchemaTree(SchemaKey.anonymousKey(), schema);

        checker.checkSyntax(pointers, BUNDLE, report, tree);

        final List<JsonPointer> expected = Lists.newArrayList();
        for (final JsonNode node: expectedPointers)
            expected.add(new JsonPointer(node.textValue()));

        assertEquals(pointers, expected);
    }

    /*
     * Utility methods
     */
    private static SchemaTree treeFromValue(final String keyword,
        final JsonNode node)
    {
        final ObjectNode schema = JacksonUtils.nodeFactory().objectNode();
        schema.set(keyword, node);
        return new CanonicalSchemaTree(SchemaKey.anonymousKey(), schema);
    }

    private static String buildMessage(final String key, final JsonNode params,
        final JsonNode data)
    {
        final ProcessingMessage message = new ProcessingMessage()
            .setMessage(BUNDLE.getMessage(key));
        if (params != null) {
            String name;
            JsonNode value;
            for (final JsonNode node: params) {
                name = node.textValue();
                value = data.get(name);
                message.putArgument(name, valueToArgument(value));
            }
        }
        return message.getMessage();
    }

    private static Object valueToArgument(final JsonNode value)
    {
        final NodeType type = NodeType.getNodeType(value);

        switch (type) {
            case STRING:
                return value.textValue();
            case INTEGER:
                return value.bigIntegerValue();
            case NUMBER: case NULL:
                return value;
            case BOOLEAN:
                return value.booleanValue();
            case ARRAY:
                final List<Object> list = Lists.newArrayList();
                for (final JsonNode element: value)
                    list.add(valueToArgument(element));
                return list;
            default:
                throw new UnsupportedOperationException();
        }
    }
}
