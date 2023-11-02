package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.validator.SignUpFormValidator;
import portfolio2023.runningmate.service.AccountService;

import javax.validation.Valid;
import java.time.LocalDateTime;

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

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);

        return "redirect:/running-mate";
    }

    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model){
        Account account = accountService.findByEmail(email);
        if (account == null){
            model.addAttribute("error", "wrong.email");
            return "account/checked-email";
        }

        if (!account.isValidToken(token)){
            model.addAttribute("error", "wrong.token");
            return "account/checked-email";
        }

        account.completeSignUp();
        accountService.login(account);
        model.addAttribute("numberOfUser", accountService.numberOfUser());
        model.addAttribute("nickname", account.getNickname());
        return "account/checked-email";
    }

}
