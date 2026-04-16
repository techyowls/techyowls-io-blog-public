package io.techyowls.auditing.service;

import io.techyowls.auditing.model.Product;
import jakarta.persistence.EntityManager;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * Service for querying product revision history using Hibernate Envers.
 */
@Service
@Transactional(readOnly = true)
public class ProductAuditService {

    private final EntityManager entityManager;

    public ProductAuditService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Get all revisions of a product.
     */
    public List<ProductRevision> getProductHistory(Long productId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        List<Number> revisions = auditReader.getRevisions(Product.class, productId);

        return revisions.stream()
            .map(rev -> {
                Product product = auditReader.find(Product.class, productId, rev);
                DefaultRevisionEntity revEntity = auditReader.findRevision(
                    DefaultRevisionEntity.class, rev
                );
                return new ProductRevision(
                    product,
                    revEntity.getRevisionDate(),
                    rev.intValue()
                );
            })
            .toList();
    }

    /**
     * Get product state at a specific revision.
     */
    public Product getProductAtRevision(Long productId, int revision) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        return auditReader.find(Product.class, productId, revision);
    }

    /**
     * Get product state at a specific point in time.
     */
    public Product getProductAtDate(Long productId, Date pointInTime) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        Number revisionNumber = auditReader.getRevisionNumberForDate(pointInTime);
        return auditReader.find(Product.class, productId, revisionNumber);
    }

    public record ProductRevision(Product product, Date timestamp, int revision) {}
}
