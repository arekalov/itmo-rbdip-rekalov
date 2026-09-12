package com.rbdip.bookstore.customer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonNameTest {

    @Test
    void splitsOnFirstSpace() {
        PersonName name = PersonName.fromFullName("Ivan Petrov");

        assertThat(name.firstName()).isEqualTo("Ivan");
        assertThat(name.lastName()).isEqualTo("Petrov");
    }

    @Test
    void keepsEverythingAfterFirstSpaceAsLastName() {
        PersonName name = PersonName.fromFullName("Anna Maria Sidorova");

        assertThat(name.firstName()).isEqualTo("Anna");
        assertThat(name.lastName()).isEqualTo("Maria Sidorova");
    }

    @Test
    void singleWordNameHasNoLastName() {
        PersonName name = PersonName.fromFullName("Madonna");

        assertThat(name.firstName()).isEqualTo("Madonna");
        assertThat(name.lastName()).isNull();
        assertThat(name.toFullName()).isEqualTo("Madonna");
    }

    @Test
    void joinsFirstAndLastNameBack() {
        assertThat(new PersonName("Ivan", "Petrov").toFullName()).isEqualTo("Ivan Petrov");
    }

    @Test
    void roundTripPreservesOriginalString() {
        for (String original : new String[] {"Ivan Petrov", "Anna Maria Sidorova", "Madonna", " Ivan Petrov"}) {
            assertThat(PersonName.fromFullName(original).toFullName()).isEqualTo(original);
        }
    }
}
