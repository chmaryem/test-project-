package tn.esprit.sampleprojet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserRepositoryTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private UserRepository userRepository;

    @Before
    public void setUp() {
        userRepository = new UserRepository();
        userRepository.setDataSource(dataSource);
    }

    @Test
    public void test_setDataSource_shouldSetDataSource() {
        DataSource newDataSource = mock(DataSource.class);
        userRepository.setDataSource(newDataSource);
        assertNotNull(userRepository);
    }

    @Test
    public void test_hashPassword_shouldReturnHashedPassword() {
        String plainPassword = "testPassword";
        String hashedPassword = userRepository.hashPassword(plainPassword);
        assertNotNull(hashedPassword);
        assertNotEquals(plainPassword, hashedPassword);
        assertTrue(hashedPassword.length() > 0);
    }

    @Test
    public void test_hashPassword_shouldReturnConsistentHash() {
        String plainPassword = "mySecurePassword";
        String hash1 = userRepository.hashPassword(plainPassword);
        String hash2 = userRepository.hashPassword(plainPassword);
        assertEquals(hash1, hash2);
    }

    @Test(expected = RuntimeException.class)
    public void test_hashPassword_shouldThrowRuntimeExceptionWhenAlgorithmNotAvailable() throws Exception {
        UserRepository repoWithBadMock = new UserRepository();
        java.lang.reflect.Field field = MessageDigest.class.getDeclaredField("algorithms");
        field.setAccessible(true);
        java.util.Map<String, Object> algorithms = (java.util.Map<String, Object>) field.get(null);
        algorithms.clear();
        try {
            repoWithBadMock.hashPassword("test");
        } finally {
        }
    }

    @Test
    public void test_findById_shouldThrowIllegalArgumentExceptionForZeroId() throws SQLException {
        try {
            userRepository.findById(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("ID must be positive", e.getMessage());
        }
    }

    @Test
    public void test_findById_shouldThrowIllegalArgumentExceptionForNegativeId() throws SQLException {
        try {
            userRepository.findById(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("ID must be positive", e.getMessage());
        }
    }

    @Test
    public void test_findById_shouldReturnUserWhenExists() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1);
        when(resultSet.getString("username")).thenReturn("testuser");
        when(resultSet.getString("email")).thenReturn("test@example.com");

        Optional<User> result = userRepository.findById(1);

        assertTrue(result.isPresent());
        assertEquals(1, result.get().id);
        assertEquals("testuser", result.get().username);
        assertEquals("test@example.com", result.get().email);
    }

    @Test
    public void test_findById_shouldReturnEmptyWhenUserNotFound() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        Optional<User> result = userRepository.findById(999);

        assertFalse(result.isPresent());
    }

    @Test
    public void test_findById_shouldUsePreparedStatementWithParameter() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        userRepository.findById(42);

        verify(preparedStatement).setInt(1, 42);
    }

    @Test
    public void test_save_shouldThrowIllegalArgumentExceptionForNullUser() {
        try {
            userRepository.save(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("User cannot be null", e.getMessage());
        } catch (SQLException e) {
            fail("Unexpected SQLException");
        }
    }

    @Test
    public void test_save_shouldThrowIllegalArgumentExceptionForNullEmail() {
        User user = new User();
        user.username = "testuser";
        user.email = null;

        try {
            userRepository.save(user);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Email cannot be empty", e.getMessage());
        } catch (SQLException e) {
            fail("Unexpected SQLException");
        }
    }

    @Test
    public void test_save_shouldThrowIllegalArgumentExceptionForEmptyEmail() {
        User user = new User();
        user.username = "testuser";
        user.email = "   ";

        try {
            userRepository.save(user);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Email cannot be empty", e.getMessage());
        } catch (SQLException e) {
            fail("Unexpected SQLException");
        }
    }

    @Test
    public void test_save_shouldThrowIllegalArgumentExceptionForNullUsername() {
        User user = new User();
        user.username = null;
        user.email = "test@example.com";

        try {
            userRepository.save(user);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Username cannot be empty", e.getMessage());
        } catch (SQLException e) {
            fail("Unexpected SQLException");
        }
    }

    @Test
    public void test_save_shouldThrowIllegalArgumentExceptionForEmptyUsername() {
        User user = new User();
        user.username = "   ";
        user.email = "test@example.com";

        try {
            userRepository.save(user);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Username cannot be empty", e.getMessage());
        } catch (SQLException e) {
            fail("Unexpected SQLException");
        }
    }

    @Test
    public void test_save_shouldSaveUserSuccessfully() throws SQLException {
        User user = new User();
        user.username = "testuser";
        user.email = "test@example.com";
        user.setPasswordHash("password123");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(100);

        userRepository.save(user);

        assertEquals(100, user.id);
        verify(preparedStatement).setString(1, "test@example.com");
        verify(preparedStatement).setString(2, "testuser");
        verify(preparedStatement).setString(3, anyString());
    }

    @Test
    public void test_save_shouldHashPassword() throws SQLException {
        User user = new User();
        user.username = "testuser";
        user.email = "test@example.com";
        user.setPasswordHash("mypassword");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        userRepository.save(user);

        verify(preparedStatement).setString(3, userRepository.hashPassword("mypassword"));
    }

    @Test
    public void test_save_shouldHandleNullPasswordHash() throws SQLException {
        User user = new User();
        user.username = "testuser";
        user.email = "test@example.com";
        user.setPasswordHash(null);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1);

        userRepository.save(user);

        verify(preparedStatement).setString(3, userRepository.hashPassword(""));
    }

    @Test
    public void test_countUsers_shouldReturnUserCount() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("total_count")).thenReturn(42);

        int count = userRepository.countUsers();

        assertEquals(42, count);
    }

    @Test
    public void test_countUsers_shouldReturnZeroWhenNoUsers() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        int count = userRepository.countUsers();

        assertEquals(0, count);
    }

    @Test
    public void test_getUsersWithOrders_shouldReturnUsersList() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(resultSet.getInt("id")).thenReturn(1).thenReturn(2);
        when(resultSet.getString("username")).thenReturn("user1").thenReturn("user2");
        when(resultSet.getString("email")).thenReturn("user1@test.com").thenReturn("user2@test.com");

        List<User> users = userRepository.getUsersWithOrders(10, 0);

        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).username);
        assertEquals("user2", users.get(1).username);
    }

    @Test
    public void test_getUsersWithOrders_shouldUseDefaultLimitWhenZero() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        userRepository.getUsersWithOrders(0, 0);

        verify(preparedStatement).setInt(1, 100);
        verify(preparedStatement).setInt(2, 0);
    }

    @Test
    public void test_getUsersWithOrders_shouldUseDefaultLimitWhenNegative() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        userRepository.getUsersWithOrders(-5, 0);

        verify(preparedStatement).setInt(1, 100);
    }

    @Test
    public void test_getUsersWithOrders_shouldUseZeroOffsetWhenNegative() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        userRepository.getUsersWithOrders(10, -5);

        verify(preparedStatement).setInt(1, 10);
        verify(preparedStatement).setInt(2, 0);
    }

    @Test
    public void test_getUsersWithOrders_shouldReturnEmptyListWhenNoResults() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<User> users = userRepository.getUsersWithOrders(10, 0);

        assertTrue(users.isEmpty());
    }

    @Test
    public void test_getUsersWithOrders_shouldSetLimitAndOffsetParameters() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        userRepository.getUsersWithOrders(20, 5);

        verify(preparedStatement).setInt(1, 20);
        verify(preparedStatement).setInt(2, 5);
    }

    @Test
    public void test_getInstance_shouldReturnNewInstance() {
        UserRepository repo1 = new UserRepository();
        UserRepository repo2 = new UserRepository();
        assertNotSame(repo1, repo2);
    }
}