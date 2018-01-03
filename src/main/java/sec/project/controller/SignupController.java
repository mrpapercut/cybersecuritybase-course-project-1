package sec.project.controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class SignupController {

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("*")
    public String defaultMapping() {
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm(Model model, @RequestParam Optional<String> test) {
        if (test.isPresent()) {
            model.addAttribute("attr", test.get());
            return "test";
        }
        
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(
            @RequestParam String name, 
            @RequestParam String street, 
            @RequestParam String postcode, 
            @RequestParam String city, 
            @RequestParam String photo) {
        signupRepository.save(new Signup(name, street, postcode, city, photo));
        return "done";
    }

}
