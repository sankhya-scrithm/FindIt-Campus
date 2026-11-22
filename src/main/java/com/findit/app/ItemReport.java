package com.findit.app;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ItemReport {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String type;
    private String itemName;
    private String category;
    private String campusLocation;
    private String itemDate;
    @Column(length = 1200) private String publicDescription;
    @Column(length = 1200) private String privateDetail;
    private String reporterName;
    private String reporterContact;
    private String status = "OPEN";
    private Instant createdAt = Instant.now();
    @Column(length=5242880) private byte[] image;
    private String imageContentType;

    public Long getId(){return id;} public String getType(){return type;} public void setType(String v){type=v;}
    public String getItemName(){return itemName;} public void setItemName(String v){itemName=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public String getCampusLocation(){return campusLocation;} public void setCampusLocation(String v){campusLocation=v;}
    public String getItemDate(){return itemDate;} public void setItemDate(String v){itemDate=v;}
    public String getPublicDescription(){return publicDescription;} public void setPublicDescription(String v){publicDescription=v;}
    public String getPrivateDetail(){return privateDetail;} public void setPrivateDetail(String v){privateDetail=v;}
    public String getReporterName(){return reporterName;} public void setReporterName(String v){reporterName=v;}
    public String getReporterContact(){return reporterContact;} public void setReporterContact(String v){reporterContact=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public Instant getCreatedAt(){return createdAt;}
    public byte[] getImage(){return image;} public void setImage(byte[] v){image=v;}
    public String getImageContentType(){return imageContentType;} public void setImageContentType(String v){imageContentType=v;}
}
