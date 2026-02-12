package github.maxsuel.agregadordeinvestimentos.entity;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {
    
    @Id
    @Column(name = "account_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID accountId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @OneToOne(cascade = CascadeType.ALL, mappedBy = "account")
    @PrimaryKeyJoinColumn
    private BillingAddress billingAddress;

    @Column(name = "description", nullable = false)
    private String description;

    @OneToMany(mappedBy = "account")
    private List<AccountStock> accountStocks;

    public Account(User user, String description, List<AccountStock> accountStocks) {
        this.user = user;
        this.description = description;
        this.accountStocks = accountStocks;
    }

}
