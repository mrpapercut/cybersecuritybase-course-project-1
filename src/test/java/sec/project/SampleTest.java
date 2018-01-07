package sec.project;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import sec.project.repository.SignupRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SampleTest {

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private SignupRepository signupRepository;

    private MockMvc mockMvc;

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webAppContext).build();
    }

    @Test
    public void signupAddsDataToDatabase() throws Throwable {
        mockMvc.perform(
                post("/form")
                    .param("email", "test@test.com")
                    .param("password", "p4ssw0rd")
                    .param("name", "Testname")
                    .param("street", "Teststreet")
                    .param("postcode", "Testpostcode")
                    .param("city", "Testcity"))
                .andReturn();
        assertEquals(1L, signupRepository.findAll().stream().filter(s -> 
                s.getEmail().equals("test@test.com") &&
                s.getPassword().equals("p4ssw0rd") &&
                s.getName().equals("Testname") &&
                s.getStreet().equals("Teststreet") &&
                s.getPostcode().equals("Testpostcode") &&
                s.getCity().equals("Testcity")
        ).count());
    }
}
