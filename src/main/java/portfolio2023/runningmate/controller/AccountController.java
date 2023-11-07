package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.domain.dto.SignUpForm;
import portfolio2023.runningmate.domain.validator.SignUpFormValidator;
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
    public String runningMateHome(@CurrentAccount Account account, Model model){
        model.addAttribute("account", account);
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

    @GetMapping("/check-email")
    public String checkEmail(@CurrentAccount Account account, Model model){
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentAccount Account account, Model model){
        if (!account.canSendConfirmEmail()){
            model.addAttribute("error", "인증 이메일은 5분에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/running-mate";
    }

    @GetMapping("/login")
    public String login(){

        return "account/login";
    }

    @GetMapping("/profile/{nickname}")
    public String viewProfile(@PathVariable String nickname, Model model, @CurrentAccount Account account){
        Account byNickname = accountService.findByNickname(nickname);
        if (nickname == null){
            throw new IllegalArgumentException(nickname + "에 해당하는 사용자가 없습니다.");
        }

        model.addAttribute("account", byNickname);
        model.addAttribute("isOwner", byNickname.equals(account));
        return "account/profile";
    }

    @GetMapping("/email-login")
    public String emailLoginForm(){
        return "account/email-login";
    }

    @PostMapping("/email-login")
    public String sendEmailLoginLink(String email, Model model, RedirectAttributes attributes){
        Account account = accountService.findByEmail(email);
        if (account == null){
            model.addAttribute("error", "유효한 이메일 주소가 아닙니다.");
            return "account/email-login";
        }

        if (!account.canSendConfirmEmail()){
            model.addAttribute("error", "이메일 로그인은 5분 뒤에 사용할 수 있습니다.");
            return "account/email-login";
        }

        accountService.sendLoginLink(account);
        attributes.addFlashAttribute("message", "이메일 인증 메일을 발송했습니다.");
        return "redirect:/running-mate/email-login";
    }

    @GetMapping("/login-by-email")
    public String loginByEmail(String token, String email, Model model){
        Account account = accountService.findByEmail(email);
        if (account == null || !account.isValidToken(token)){
            model.addAttribute("error", "로그인 할 수 없습니다.");
            return "account/logged-in-by-email";
        }

        accountService.login(account);
        return "account/logged-in-by-email";
    }
}
