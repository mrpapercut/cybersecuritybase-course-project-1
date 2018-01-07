package sec.project.controller;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import sec.project.domain.Signup;
import sec.project.repository.SignupRepository;

@Controller
public class SignupController {
    
    private void validateCookieuid(Long cookieuid, ModelMap model) {
        if (cookieuid != 0) {
            Signup user = signupRepository.findOne(cookieuid);
            
            if (user != null) {
                model.addAttribute("cookieuid", cookieuid);
            }
        }
    }

    @Autowired
    private SignupRepository signupRepository;

    @RequestMapping("*")
    public String defaultMapping(@CookieValue(value = "userid", defaultValue = "0") Long userid) {
        if (userid != 0) {
            return "redirect:/user/" + userid;
        }
        
        return "redirect:/form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.GET)
    public String loadForm(
            ModelMap model,
            @CookieValue(value = "cookieuid", defaultValue = "0") Long cookieuid) {
        
        this.validateCookieuid(cookieuid, model);                
        
        return "form";
    }

    @RequestMapping(value = "/form", method = RequestMethod.POST)
    public String submitForm(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String name, 
            @RequestParam String street, 
            @RequestParam String postcode, 
            @RequestParam String city,
            HttpServletResponse response) {
        
        Long id = signupRepository.save(new Signup(email, password, name, street, postcode, city)).getId();
        
        Cookie cookie = new Cookie("cookieuid", id.toString());
        cookie.setMaxAge(3600);
        response.addCookie(cookie);
                
        return "redirect:/user/" + id;
    }
    
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public String usersOverview(
            ModelMap model, 
            @CookieValue(value = "cookieuid", defaultValue = "0") Long cookieuid) {
        
        this.validateCookieuid(cookieuid, model);
        
        List<Signup> users = signupRepository.findAll();
        
        model.addAttribute("users", users);
        
        return "users";
    }
    
    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
    public String accountDetails(
            ModelMap model, 
            @PathVariable Long id,
            @CookieValue(value = "cookieuid", defaultValue = "0") Long cookieuid) {
        
        this.validateCookieuid(cookieuid, model);   
        
        Signup user = signupRepository.findOne(id);
        
        if (user == null) {
            return "redirect:../form";
        }
        
        model.addAttribute("user", user);
        
        return "user";
    }
    
    @RequestMapping(value = "/user/{id}", method = RequestMethod.POST)
    public String updateAccountDetails(
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String name, 
            @RequestParam String street, 
            @RequestParam String postcode, 
            @RequestParam String city,
            @PathVariable Long id) {
        
        Signup user = signupRepository.findOne(id);
        
        if (user == null) {
            return "redirect:../form";
        }
        
        user.setEmail(email);
        user.setPassword(password);
        user.setName(name);
        user.setStreet(street);
        user.setPostcode(postcode);
        user.setCity(city);
        
        signupRepository.save(user);
        
        return "redirect:/user/" + id;
    }
    
    @RequestMapping (value = "/logout", method = RequestMethod.GET)
    public String logoutUser(HttpServletRequest request, HttpServletResponse response) throws MalformedURLException {
        String referer = new URL(request.getHeader("referer")).getPath();
        
        Cookie cookie = new Cookie("cookieuid", "0");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        
        return "redirect:/login?nextpage=" + referer;
    }
    
    @RequestMapping (value = "/login", method = RequestMethod.GET)
    public String loginUserPage(ModelMap model, @RequestParam Optional<String> nextpage) {
        if (nextpage.isPresent()) {
            model.addAttribute("nextpage", nextpage.get());
        }
        
        return "login";
    }
    
    @RequestMapping (value = "/login", method = RequestMethod.POST)
    public String loginUser(ModelMap model, HttpServletRequest request, String email, String password, String nextpage) {
        Signup user = signupRepository.findByEmailAndPassword(email, password);
        
        if (user != null) {
            if (nextpage != "") {
                return "redirect:" + nextpage;
            } else {
                return "redirect:/user/" + user.getId();
            }
        } else {            
            return "redirect:" + request.getRequestURI();
        }
    }
}
