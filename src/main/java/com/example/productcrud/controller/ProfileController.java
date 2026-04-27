package com.example.productcrud.controller;

import com.example.productcrud.dto.ChangePasswordRequest;
import com.example.productcrud.dto.EditProfileRequest;
import com.example.productcrud.model.User;
import com.example.productcrud.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent()) {
            model.addAttribute("user", optionalUser.get());
        }

        return "profile";
    }

    @GetMapping("/profile/edit")
    public String showEditProfileForm(Authentication authentication, Model model) {
        String username = authentication.getName();
        Optional<User> optionalUser = userService.findByUsername(username);

        EditProfileRequest request = new EditProfileRequest();

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            request.setFullName(user.getFullName());
            request.setEmail(user.getEmail());
            request.setPhoneNumber(user.getPhoneNumber());
            request.setAddress(user.getAddress());
            request.setBio(user.getBio());
            request.setProfileImageUrl(user.getProfileImageUrl());
        }

        model.addAttribute("editProfileRequest", request);
        return "edit-profile";
    }

    @PostMapping("/profile/edit")
    public String processEditProfile(
            @ModelAttribute("editProfileRequest") EditProfileRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        userService.updateProfile(username, request);

        redirectAttributes.addFlashAttribute("success", "Profil berhasil diperbarui!");
        return "redirect:/profile";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordForm(Model model) {
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        return "change-password";
    }

    @PostMapping("/profile/change-password")
    public String processChangePassword(
            @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        if (request.getOldPassword() == null || request.getOldPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password lama tidak boleh kosong");
            return "redirect:/profile/change-password";
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Password baru tidak boleh kosong");
            return "redirect:/profile/change-password";
        }

        if (request.getConfirmNewPassword() == null || request.getConfirmNewPassword().trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Konfirmasi password tidak boleh kosong");
            return "redirect:/profile/change-password";
        }

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            redirectAttributes.addFlashAttribute("error", "Password baru dan konfirmasi password harus sama");
            return "redirect:/profile/change-password";
        }

        String username = authentication.getName();
        boolean berhasil = userService.changePassword(username, request.getOldPassword(), request.getNewPassword());

        if (!berhasil) {
            redirectAttributes.addFlashAttribute("error", "Password lama tidak sesuai");
            return "redirect:/profile/change-password";
        }

        redirectAttributes.addFlashAttribute("success", "Password berhasil diubah!");
        return "redirect:/profile";
    }
}