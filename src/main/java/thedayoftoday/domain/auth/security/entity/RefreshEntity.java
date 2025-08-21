package thedayoftoday.domain.auth.security.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import thedayoftoday.domain.common.BaseEntity;

@Entity
@Getter
@Setter
public class RefreshEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String refresh;
    private String expiration;
}
