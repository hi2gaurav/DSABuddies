package com.dsabuddies.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = {
        "/{path:[^\\.]*}",
        "/tasks/**",
        "/profile/**",
        "/admin/**"
    })
    public String redirect() {
        return "forward:/index.html";
    }
}
