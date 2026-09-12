package com.rbdip.bookstore.migration;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Нагрузочный тест ЛР3: expand-шаг разбиения customers.full_name
 * (V3__split_customer_full_name.sql) накатывается, пока "старая версия
 * приложения" продолжает читать и писать колонку full_name. Ошибок быть
 * не должно, а данные, записанные до миграции, должны быть корректно
 * разнесены по first_name/last_name.
 *
 * <p>Тест работает на уровне JDBC, а не HTTP: приложение стартует с
 * ddl-auto=validate против финальной схемы и не может быть поднято на
 * промежуточной версии, поэтому "старые" потребители эмулируются
 * прямыми SQL-запросами - так же, как в эталонном
 * LoadDuringMigrationReferenceTest.
 */
@Testcontainers
class LoadDuringNameSplitTest {

    private static final int WORKER_THREADS = 4;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("bookstore_name_split")
            .withUsername("bookstore")
            .withPassword("bookstore");

    @Test
    void expandStepSurvivesOldReadersAndWriters() throws Exception {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgres.getJdbcUrl());
        dataSource.setUser(postgres.getUsername());
        dataSource.setPassword(postgres.getPassword());

        // Схема до разбиения имени: customers с колонкой full_name.
        Flyway.configure().dataSource(dataSource).target("2").load().migrate();

        try (Connection c = dataSource.getConnection();
                Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO customers (full_name, address) VALUES ('Ivan Petrov', 'Moscow')");
            st.executeUpdate("INSERT INTO customers (full_name) VALUES ('Madonna')");
        }

        List<Exception> errors = new CopyOnWriteArrayList<>();
        AtomicBoolean keepRunning = new AtomicBoolean(true);
        AtomicInteger writes = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(WORKER_THREADS);
        ExecutorService pool = Executors.newFixedThreadPool(WORKER_THREADS);

        Runnable oldReader = () -> {
            started.countDown();
            while (keepRunning.get()) {
                try (Connection c = dataSource.getConnection();
                        Statement st = c.createStatement()) {
                    st.executeQuery("SELECT full_name FROM customers LIMIT 1");
                } catch (SQLException e) {
                    errors.add(e);
                }
            }
        };
        Runnable oldWriter = () -> {
            started.countDown();
            while (keepRunning.get()) {
                try (Connection c = dataSource.getConnection();
                        Statement st = c.createStatement()) {
                    st.executeUpdate("INSERT INTO customers (full_name) VALUES ('Load Test')");
                    writes.incrementAndGet();
                } catch (SQLException e) {
                    errors.add(e);
                }
            }
        };
        pool.submit(oldReader);
        pool.submit(oldReader);
        pool.submit(oldWriter);
        pool.submit(oldWriter);
        started.await(5, TimeUnit.SECONDS);

        // Expand-шаг накатывается под нагрузкой от "старых" потребителей.
        Flyway.configure().dataSource(dataSource).target("3").load().migrate();

        keepRunning.set(false);
        pool.shutdown();
        pool.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(errors)
                .as("Чтения и записи full_name не должны падать во время expand-шага")
                .isEmpty();
        assertThat(writes.get())
                .as("Писатели должны были успеть сделать хотя бы одну запись")
                .isPositive();

        try (Connection c = dataSource.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT first_name, last_name FROM customers WHERE full_name = 'Ivan Petrov'")) {
            assertThat(rs.next()).isTrue();
            assertThat(Map.of("first", rs.getString(1), "last", rs.getString(2)))
                    .containsEntry("first", "Ivan")
                    .containsEntry("last", "Petrov");
        }

        try (Connection c = dataSource.getConnection();
                Statement st = c.createStatement();
                ResultSet rs = st.executeQuery(
                        "SELECT first_name, last_name FROM customers WHERE full_name = 'Madonna'")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getString(1)).isEqualTo("Madonna");
            assertThat(rs.getString(2)).isNull();
        }

        // Старый способ записи работает и после expand-шага: колонка
        // full_name ещё на месте, NOT NULL с неё снят.
        try (Connection c = dataSource.getConnection();
                Statement st = c.createStatement()) {
            st.executeUpdate("INSERT INTO customers (full_name) VALUES ('After Expand')");
        }
    }
}
