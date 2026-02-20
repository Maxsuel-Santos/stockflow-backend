package github.maxsuel.agregadordeinvestimentos.entity;

import github.maxsuel.agregadordeinvestimentos.entity.enums.TradeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transactions {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    @Enumerated(EnumType.STRING)
    private TradeType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "price_at_time", precision = 19, scale = 4, nullable = false)
    private BigDecimal priceAtTime;

    @CreationTimestamp
    private Instant timestamp;

}
