package com.findit.app;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ReportRepository extends JpaRepository<ItemReport,Long> { List<ItemReport> findAllByOrderByCreatedAtDesc(); }
