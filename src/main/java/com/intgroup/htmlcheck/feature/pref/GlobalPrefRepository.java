package com.intgroup.htmlcheck.feature.pref;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GlobalPrefRepository extends JpaRepository<GlobalPref, String> {
}
