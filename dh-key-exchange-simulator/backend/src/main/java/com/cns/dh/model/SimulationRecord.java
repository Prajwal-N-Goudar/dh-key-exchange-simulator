package com.cns.dh.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "simulation_history")
public class SimulationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String mode;

    @Column(nullable = false, length = 40)
    private String p;

    @Column(nullable = false, length = 40)
    private String g;

    @Column(nullable = false, length = 100)
    private String alicePublic;

    @Column(nullable = false, length = 100)
    private String bobPublic;

    @Column(nullable = false)
    private boolean secretsMatch;

    @Column(nullable = false)
    private Instant createdAt;

    protected SimulationRecord() {}

    public SimulationRecord(String mode, String p, String g, String alicePublic,
                            String bobPublic, boolean secretsMatch) {
        this.mode = mode;
        this.p = p;
        this.g = g;
        this.alicePublic = alicePublic;
        this.bobPublic = bobPublic;
        this.secretsMatch = secretsMatch;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getMode() { return mode; }
    public String getP() { return p; }
    public String getG() { return g; }
    public String getAlicePublic() { return alicePublic; }
    public String getBobPublic() { return bobPublic; }
    public boolean isSecretsMatch() { return secretsMatch; }
    public Instant getCreatedAt() { return createdAt; }
}
