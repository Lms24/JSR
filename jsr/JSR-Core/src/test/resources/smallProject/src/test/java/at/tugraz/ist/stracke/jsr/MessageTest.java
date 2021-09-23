package at.tugraz.ist.stracke.jsr;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageTest {

    @Test
    public void testHashCode() {
        Message msg = new Message("Joe", "hi", "Hello Joe");
        assertTrue(0 != msg.hashCode());
    }

    @Test
    public void testHashCode2() {
        // this TC is redundant
        Message msg = new Message("Joe2", "hi2", "Hello Joe2");
        assertTrue(0 != msg.hashCode());
    }

    @Test
    public void testEquals() {
        Message msg1 = new Message("Joe", "hi", "Hello Joe");

        assertTrue(msg1.equals(msg1));
    }

    @Test
    public void testEquals2() {
        Message msg1 = new Message("Joe", "hi", "Hello Joe");
        Message msg2 = new Message("Jane", "hi there", "Hello Jane");
        Message msg3 = new Message("Joe", "hi", "Hello Joe");

        assertTrue(msg1.equals(msg1));
        assertTrue(msg1.equals(msg3));
        assertFalse(msg2.equals(msg1));
        assertTrue(!msg2.equals(msg3));
        assertNotNull(msg1);
    }

    @Test
    public void testToString() {
        Message msg1 = new Message("Joe", "hi", "Hello Joe");

        String expected = "{\n" +
            "  recipient: Joe,\n" +
            "  subject: hi,\n" +
            "  message: Hello Joe\n" +
            "}";

        Assert.assertEquals(expected, msg1.toString());
    }

    @Test
    public void testGetters() {
        Message msg1 = new Message("Joe", "hi", "Hello Joe");
        assertEquals("Joe", msg1.getRecipient());
        assertEquals("hi", msg1.getSubject());
        assertEquals("Hello Joe", msg1.getMsg());
    }
}