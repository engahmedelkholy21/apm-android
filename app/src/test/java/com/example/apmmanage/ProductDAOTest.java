package com.example.apmmanage;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ProductDAOTest {
    @Mock
    Connection connection;
    @Mock
    PreparedStatement statement;
    @Mock
    ResultSet resultSet;

    AddProductActivity.ProductDAO dao;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.openMocks(this);
        dao = new AddProductActivity.ProductDAO(connection);
        when(connection.prepareStatement(any(String.class))).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
    }

    @Test
    public void isProductExists_true_whenResultHasRow() throws Exception {
        when(resultSet.next()).thenReturn(true);

        boolean exists = dao.isProductExists("name", "stock");

        assertTrue(exists);
        verify(statement).setString(1, "name");
        verify(statement).setString(2, "stock");
        verify(resultSet).close();
        verify(statement).close();
    }

    @Test
    public void isProductExists_false_whenResultEmpty() throws Exception {
        when(resultSet.next()).thenReturn(false);

        boolean exists = dao.isProductExists("name", "stock");

        assertFalse(exists);
    }
}
