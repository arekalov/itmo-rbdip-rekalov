package com.rbdip.bookstore.customer;

/**
 * Имя клиента, разобранное на имя и фамилию (ЛР3): первое слово -
 * имя, всё после первого пробела - фамилия. Разбор и сборка взаимно
 * обратны: toFullName() возвращает исходную строку.
 */
public record PersonName(String firstName, String lastName) {

    public static PersonName fromFullName(String fullName) {
        int firstSpace = fullName.indexOf(' ');
        if (firstSpace < 0) {
            return new PersonName(fullName, null);
        }
        return new PersonName(fullName.substring(0, firstSpace), fullName.substring(firstSpace + 1));
    }

    public String toFullName() {
        return lastName == null ? firstName : firstName + " " + lastName;
    }
}
