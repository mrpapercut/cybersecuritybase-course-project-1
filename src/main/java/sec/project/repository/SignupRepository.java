package sec.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import sec.project.domain.Signup;

public interface SignupRepository extends JpaRepository<Signup, Long> {

    @Query("SELECT u FROM Signup u WHERE u.email = ?1 AND u.password = ?2")
    Signup findByEmailAndPassword(String email, String password);
}
