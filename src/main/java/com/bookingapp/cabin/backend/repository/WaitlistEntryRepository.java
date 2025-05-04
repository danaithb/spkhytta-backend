//Source:https://github.com/jlwcrews2/alexandria/blob/main/src/main/java/no/jlwcrews/alexandria/location/LocationRepo.java

package com.bookingapp.cabin.backend.repository;

import com.bookingapp.cabin.backend.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
}
