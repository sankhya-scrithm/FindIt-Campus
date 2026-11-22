package com.findit.app;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class Claim {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long reportId;
    private String claimantName;
    private String claimantContact;
    @Column(length=1500) private String ownershipEvidence;
    private String status = "PENDING";
    private Instant createdAt = Instant.now();
    @Column(length=1000) private String reviewerNote;
    public Long getId(){return id;} public Long getReportId(){return reportId;} public void setReportId(Long v){reportId=v;}
    public String getClaimantName(){return claimantName;} public void setClaimantName(String v){claimantName=v;}
    public String getClaimantContact(){return claimantContact;} public void setClaimantContact(String v){claimantContact=v;}
    public String getOwnershipEvidence(){return ownershipEvidence;} public void setOwnershipEvidence(String v){ownershipEvidence=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getCreatedAt(){return createdAt;} public String getReviewerNote(){return reviewerNote;} public void setReviewerNote(String v){reviewerNote=v;}
}
