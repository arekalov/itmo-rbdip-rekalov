package com.rbdip.bookstore.review;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Отзывы на товары. Факт покупки проверяется через явный контракт
 * {@link PurchaseVerifier} - модуль review не зависит от внутренних
 * классов модуля order (ЛР4, Strangler Fig).
 */
@Service
public class ReviewService {

    private static final Logger LOG = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final PurchaseVerifier purchaseVerifier;

    public ReviewService(ReviewRepository reviewRepository, PurchaseVerifier purchaseVerifier) {
        this.reviewRepository = reviewRepository;
        this.purchaseVerifier = purchaseVerifier;
    }

    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        boolean verifiedPurchase = purchaseVerifier.isVerifiedPurchase(productId);
        LOG.debug("review for product {}: verified purchase = {}", productId, verifiedPurchase);
        Review review = new Review(productId, authorName == null ? "anonymous" : authorName, rating, comment);
        return reviewRepository.save(review);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
