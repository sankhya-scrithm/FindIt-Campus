package com.findit.app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api")
public class FindItController {
    private final ReportRepository reports;
    private final ClaimRepository claims;
    @Value("${findit.admin-key}") private String adminKey;

    public FindItController(ReportRepository reports, ClaimRepository claims) { this.reports=reports; this.claims=claims; }

    private void requireAdmin(String key) { if (key == null || key.isBlank() || !java.security.MessageDigest.isEqual(key.getBytes(), adminKey.getBytes())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED,"Reviewer access required"); }
    private Map<String,Object> publicReport(ItemReport r) {
        Map<String,Object> m=new LinkedHashMap<>();
        m.put("id",r.getId()); m.put("type",r.getType()); m.put("itemName",r.getItemName()); m.put("category",r.getCategory());
        m.put("campusLocation",r.getCampusLocation()); m.put("itemDate",r.getItemDate()); m.put("publicDescription",r.getPublicDescription());
        m.put("status",r.getStatus()); m.put("createdAt",r.getCreatedAt()); m.put("hasImage",r.getImage()!=null && r.getImage().length>0); return m;
    }
    @GetMapping("/reports") public List<Map<String,Object>> allReports() { return reports.findAllByOrderByCreatedAtDesc().stream().map(this::publicReport).toList(); }
    @PostMapping(value="/reports", consumes=MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String,Object> createReport(@RequestParam String type, @RequestParam String itemName,
        @RequestParam String category, @RequestParam String campusLocation, @RequestParam String itemDate,
        @RequestParam String publicDescription, @RequestParam String privateDetail,
        @RequestParam String reporterName, @RequestParam String reporterContact,
        @RequestParam(required=false) MultipartFile image) throws IOException {
        if (!Set.of("LOST","FOUND").contains(type.toUpperCase())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Type must be LOST or FOUND");
        if (itemName.isBlank() || privateDetail.isBlank() || reporterName.isBlank() || reporterContact.isBlank()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Complete all required fields");
        ItemReport r=new ItemReport(); r.setType(type.toUpperCase()); r.setItemName(itemName.trim()); r.setCategory(category); r.setCampusLocation(campusLocation.trim());
        r.setItemDate(itemDate); r.setPublicDescription(publicDescription); r.setPrivateDetail(privateDetail); r.setReporterName(reporterName.trim()); r.setReporterContact(reporterContact.trim());
        if(image!=null && !image.isEmpty()) { String ct=image.getContentType(); if(ct==null || !Set.of("image/jpeg","image/png","image/gif","image/webp").contains(ct)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Upload a JPG, PNG, GIF or WEBP image"); r.setImage(image.getBytes()); r.setImageContentType(ct); }
        return publicReport(reports.save(r));
    }
    @GetMapping("/reports/{id}/image") public ResponseEntity<byte[]> image(@PathVariable Long id) {
        ItemReport r=reports.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        if(r.getImage()==null) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(r.getImageContentType())).cacheControl(CacheControl.noCache()).body(r.getImage());
    }
    @PostMapping("/reports/{id}/claims") public Map<String,Object> createClaim(@PathVariable Long id,@RequestBody Map<String,String> body) {
        ItemReport r=reports.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND,"Report not found"));
        if(!"OPEN".equals(r.getStatus())) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.CONFLICT,"This report is no longer open");
        String name=body.getOrDefault("claimantName","").trim(), contact=body.getOrDefault("claimantContact","").trim(), evidence=body.getOrDefault("ownershipEvidence","").trim();
        if(name.isBlank()||contact.isBlank()||evidence.isBlank()) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Complete all claim fields");
        Claim c=new Claim(); c.setReportId(id); c.setClaimantName(name); c.setClaimantContact(contact); c.setOwnershipEvidence(evidence); c=claims.save(c);
        return Map.of("id",c.getId(),"status",c.getStatus(),"message","Your claim has been submitted for manual review.");
    }
    @GetMapping("/admin/reports") public List<Map<String,Object>> adminReports(@RequestHeader(value="X-Admin-Key",required=false) String key) {
        requireAdmin(key); return reports.findAllByOrderByCreatedAtDesc().stream().map(r->{Map<String,Object> m=publicReport(r);m.put("privateDetail",r.getPrivateDetail());m.put("reporterName",r.getReporterName());m.put("reporterContact",r.getReporterContact());return m;}).toList();
    }
    @GetMapping("/admin/claims") public List<Map<String,Object>> adminClaims(@RequestHeader(value="X-Admin-Key",required=false) String key) {
        requireAdmin(key); return claims.findAllByOrderByCreatedAtDesc().stream().map(c->{Map<String,Object> m=new LinkedHashMap<>();m.put("id",c.getId());m.put("reportId",c.getReportId());m.put("claimantName",c.getClaimantName());m.put("claimantContact",c.getClaimantContact());m.put("ownershipEvidence",c.getOwnershipEvidence());m.put("status",c.getStatus());m.put("createdAt",c.getCreatedAt());m.put("reviewerNote",c.getReviewerNote());return m;}).toList();
    }
    @PatchMapping("/admin/claims/{id}") public Map<String,Object> reviewClaim(@PathVariable Long id,@RequestHeader(value="X-Admin-Key",required=false) String key,@RequestBody Map<String,String> body) {
        requireAdmin(key); Claim c=claims.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        String status=body.getOrDefault("status","").toUpperCase(); if(!Set.of("APPROVED","REJECTED").contains(status)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Status must be APPROVED or REJECTED");
        c.setStatus(status); c.setReviewerNote(body.getOrDefault("reviewerNote","")); claims.save(c);
        if("APPROVED".equals(status)) reports.findById(c.getReportId()).ifPresent(r->{r.setStatus("RETURN_PENDING");reports.save(r);});
        return Map.of("id",c.getId(),"status",c.getStatus());
    }
    @PatchMapping("/admin/reports/{id}/returned") public Map<String,Object> markReturned(@PathVariable Long id,@RequestHeader(value="X-Admin-Key",required=false) String key) {
        requireAdmin(key); ItemReport r=reports.findById(id).orElseThrow(()->new org.springframework.web.server.ResponseStatusException(HttpStatus.NOT_FOUND));
        r.setStatus("RETURNED"); reports.save(r); return Map.of("id",id,"status",r.getStatus());
    }
}
