package com.framework.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * JsonDataReader — Supplies test data to TestNG @DataProvider from JSON files.
 *
 * JSON format:
 * [
 *   { "username": "user@test.com", "password": "Pass1!", "expectedResult": "success" }
 * ]
 */
public class JsonDataReader {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonDataReader() {}

    public static Object[][] readData(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new RuntimeException(
                "Test data file not found: " + file.getAbsolutePath() + 
                ". Ensure JSON file exists at the specified path.");
        }

        try {
            List<Map<String, String>> data = mapper.readValue(
                file,
                new TypeReference<List<Map<String, String>>>() {});

            Object[][] result = new Object[data.size()][1];
            for (int i = 0; i < data.size(); i++) {
                result[i][0] = data.get(i);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(
                "Failed to parse test data from: " + file.getAbsolutePath(), e);
        }
    }

    public static <T> List<T> readList(String filePath, Class<T> clazz) throws Exception {
        return mapper.readValue(new File(filePath),
            mapper.getTypeFactory().constructCollectionType(List.class, clazz));
    }
}
