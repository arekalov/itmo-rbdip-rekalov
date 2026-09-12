package com.rbdip.bookstore.review;

/**
 * Контракт модуля review к "внешнему миру" (Strangler Fig, ЛР4):
 * отзывам нужно знать, была ли покупка, но не внутреннее устройство
 * модуля заказов. Реализация живёт на стороне модуля order.
 */
public interface PurchaseVerifier {

    boolean isVerifiedPurchase(Long productId);
}
