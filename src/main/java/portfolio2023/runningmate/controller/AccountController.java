package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.validator.SignUpFormValidator;
import portfolio2023.runningmate.repository.AccountRepository;
import portfolio2023.runningmate.service.AccountService;

import javax.validation.Valid;

@Controller
@RequestMapping("/running-mate")
@RequiredArgsConstructor
public class AccountController {

    private final SignUpFormValidator signUpFormValidator;
    private final AccountService accountService;

    @InitBinder("signUpForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(signUpFormValidator);
    }

    @GetMapping
    public String runningMateHome(){
        return "index";
    }

    @GetMapping("/sign-up")
    public String signUpForm(Model model){
        model.addAttribute("signUpForm", new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
        if (errors.hasErrors()){
            return "account/sign-up";
        }

        accountService.processNewAccount(signUpForm);

        return "redirect:/running-mate";
    }

}
