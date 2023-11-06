package portfolio2023.runningmate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import portfolio2023.runningmate.domain.Account;
import portfolio2023.runningmate.domain.dto.PasswordForm;
import portfolio2023.runningmate.domain.dto.Profile;
import portfolio2023.runningmate.domain.validator.PasswordFormValidator;
import portfolio2023.runningmate.security.CurrentAccount;
import portfolio2023.runningmate.service.AccountService;

import javax.validation.Valid;

@Controller
@RequestMapping("/running-mate")
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;

    @InitBinder("passwordForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @GetMapping("/settings/profile")
    public String updateProfileForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        return "settings/update-profile";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile, Errors errors,
                                Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/update-profile";
        }

        accountService.updateProfile(account,profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/running-mate/settings/profile";
    }

    @GetMapping("/settings/password")
    public String updatePasswordForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "settings/update-password";
    }

    @PostMapping("/settings/password")
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes){

        if (errors.hasErrors()){
            model.addAttribute(account);
            return "settings/update-password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/running-mate/settings/password";
    }
}
