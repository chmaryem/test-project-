package tn.esprit.sampleprojet;

import java.util.*;
import java.text.SimpleDateFormat;

/**
 * DatabaseHelper - Utility class with many problems
 * INTENTIONAL BUGS FOR TESTING
 */
public class DatabaseHelper {

    // PROBLEM 1: Utility class should have private constructor
    // This class can be instantiated but shouldn't be!

    // PROBLEM 2: Non-thread-safe SimpleDateFormat as static field
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // PROBLEM 3: Mutable static collection
    public static List<String> queryLog = new ArrayList<>();

    // PROBLEM 4: Magic strings scattered everywhere
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ORDERS = "orders";

    // PROBLEM 5: SQL builder with concatenation (injection risk)
    public static String buildSelectQuery(String table, String column, String value) {
        // PROBLEM 6: No validation or sanitization
        String query = "SELECT * FROM " + table +
                " WHERE " + column + " = '" + value + "'";

        // PROBLEM 7: Side effect in utility method
        queryLog.add(query);

        return query;  // CRITICAL: SQL injection!
    }

    // PROBLEM 8: Poor abstraction
    public static String buildInsertQuery(String table, Map<String, String> values) {
        StringBuilder query = new StringBuilder("INSERT INTO " + table + " (");
        StringBuilder valuePart = new StringBuilder(" VALUES (");

        // PROBLEM 9: Assumes map maintains order (it doesn't!)
        for (String key : values.keySet()) {
            query.append(key).append(",");
            valuePart.append("'").append(values.get(key)).append("',");  // PROBLEM 10: SQL injection
        }

        // PROBLEM 11: Removing last comma (fragile)
        query.deleteCharAt(query.length() - 1);
        valuePart.deleteCharAt(valuePart.length() - 1);

        query.append(")").append(valuePart).append(")");

        return query.toString();
    }

    // PROBLEM 12: Duplicate code from above
    public static String buildUpdateQuery(String table, Map<String, String> values, String idColumn, String idValue) {
        StringBuilder query = new StringBuilder("UPDATE " + table + " SET ");

        for (String key : values.keySet()) {
            query.append(key).append(" = '").append(values.get(key)).append("',");
        }

        query.deleteCharAt(query.length() - 1);
        query.append(" WHERE ").append(idColumn).append(" = '").append(idValue).append("'");

        return query.toString();  // PROBLEM 13: SQL injection everywhere
    }

    // PROBLEM 14: Method doing too many things
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";  // PROBLEM 15: Returning empty string instead of null
        }

        // PROBLEM 16: Incomplete sanitization
        input = input.replace("'", "''");  // Only escaping single quote
        input = input.trim();
        input = input.replace("<script>", "");  // PROBLEM 17: Case sensitive
        input = input.replace("SELECT", "");  // PROBLEM 18: Can be bypassed with "SeLeCt"

        // PROBLEM 19: No logging of what was sanitized

        return input;
    }

    // PROBLEM 20: Inconsistent date formatting
    public static String formatDate(Date date) {
        // PROBLEM 21: Thread-safety issue with SimpleDateFormat
        return dateFormat.format(date);
    }

    // PROBLEM 22: Exception swallowing
    public static Date parseDate(String dateStr) {
        try {
            return dateFormat.parse(dateStr);
        } catch (Exception e) {
            return null;  // PROBLEM 23: Losing error information
        }
    }

    // PROBLEM 24: Magic numbers
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {  // Why 8?
            return false;
        }

        // PROBLEM 25: Weak password rules
        return password.length() > 5;  // Different number!
    }

    // PROBLEM 26: Hardcoded configuration
    public static int getConnectionTimeout() {
        return 30000;  // Magic number, should be configurable
    }

    public static int getMaxRetries() {
        return 3;  // Another magic number
    }

    // PROBLEM 27: Static method doing I/O (hard to test)
    public static void logQuery(String query) {
        System.out.println("[SQL] " + query);  // PROBLEM 28: Using System.out for logging
        queryLog.add(query);  // PROBLEM 29: Memory leak - unbounded list
    }

    // PROBLEM 30: No way to clear query log
    // Memory will grow indefinitely!
}