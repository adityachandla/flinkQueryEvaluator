package org.tue.thesis.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QueryParserTest {

    @Test
    public void testQueryParsing() {
        String input = "Person (<>, personKnows)(>, personInterestTag)(>, tagClass)";
        var q = QueryParser.parserQuery(input, 1);
        assertEquals(q.getId(), 1);
        assertEquals(q.getSourceType(), "Person");

        assertEquals(q.getLabelDirections().size(), 3);

        assertEquals(q.getLabelDirections().get(0).getLabel(), "personKnows");
        assertEquals(q.getLabelDirections().get(0).getDirection(), Direction.BOTH);

        assertEquals(q.getLabelDirections().get(1).getLabel(), "personInterestTag");
        assertEquals(q.getLabelDirections().get(1).getDirection(), Direction.OUTGOING);

        assertEquals(q.getLabelDirections().get(2).getLabel(), "tagClass");
        assertEquals(q.getLabelDirections().get(2).getDirection(), Direction.OUTGOING);

    }

}