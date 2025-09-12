package lion.mode.tradebot_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "indicator_parameter")
@Data
public class IndicatorParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paramKey;
    private String paramValue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;

}
