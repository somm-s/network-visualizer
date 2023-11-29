package com.lockedshields;

import org.junit.Test;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class RecursivePcapConversionTaskTest {

    @Test
    public void testHasFileEnding() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Arrange
        RecursivePcapConversionTask task = new RecursivePcapConversionTask("", "", null, 0, 0);
        File fileWithEnding = new File("example.txt");
        File fileWithoutEnding = new File("example");

        // Act
        boolean resultWithEnding = invokePrivateMethod(task, "hasFileEnding", fileWithEnding, "txt");
        boolean resultWithoutEnding = invokePrivateMethod(task, "hasFileEnding", fileWithoutEnding, "txt");

        // Assert
        assertTrue(resultWithEnding);
        assertFalse(resultWithoutEnding);
    }

    @Test
    public void testHasAllowedEnding() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // Arrange
        RecursivePcapConversionTask task = new RecursivePcapConversionTask("", "", null, 0, 0);
        File fileWithAllowedEnding = new File("example.pcap.gz");
        File fileWithNotAllowedEnding = new File("example.exe");
        String[] allowedEndings = new String[]{"pcap", "pcap.gz"};

        Method method = RecursivePcapConversionTask.class.getDeclaredMethod("hasAllowedEnding", File.class, String[].class);
        method.setAccessible(true);
        boolean resultWithAllowedEnding = (boolean) method.invoke(task, fileWithAllowedEnding, allowedEndings);
        boolean resultWithNotAllowedEnding = (boolean) method.invoke(task, fileWithNotAllowedEnding, allowedEndings);

        // Assert
        assertTrue(resultWithAllowedEnding);
        assertFalse(resultWithNotAllowedEnding);
    }


    // Helper method to invoke private methods using reflection
    private boolean invokePrivateMethod(RecursivePcapConversionTask instance, String methodName, File file, String ending)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = RecursivePcapConversionTask.class.getDeclaredMethod(methodName, File.class, String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(instance, file, ending);
    }
}
