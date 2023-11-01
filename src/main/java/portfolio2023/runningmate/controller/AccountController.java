package portfolio2023.runningmate.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/running-mate")
public class AccountController {

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        return "account/sign-up";
    }
}
