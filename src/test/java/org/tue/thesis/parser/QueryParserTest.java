package org.tue.thesis.parser;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

    @Test
    public void testQueryParsing() {
        String input = "Person personKnows,personInterestTag,tagClass";
        var q = QueryParser.parserQuery(input, 1);
        assertEquals(q.getId(), 1);
        assertEquals(q.getSourceType(), "Person");

        assertEquals( 3, q.getLabels().size());
        assertEquals(List.of("personKnows", "personInterestTag", "tagClass"), q.getLabels());

    }

}