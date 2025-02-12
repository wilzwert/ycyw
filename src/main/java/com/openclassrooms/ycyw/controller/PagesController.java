package com.openclassrooms.ycyw.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Wilhelm Zwertvaegher
 * Date:11/02/2025
 * Time:16:25
 */

@Controller
public class PagesController {
    @RequestMapping("/")
    public String home() {
        return "index";
    }

    @RequestMapping("/chat")
    public String chat() {
        return "chat";
    }

    @RequestMapping("/support")
    public String support() {
        return "support";
    }

    @RequestMapping("/testStorage")
    public String testStorage() {
        return "test_storage";
    }
}
