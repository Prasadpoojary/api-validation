package com.production.apivalidation.controller;

import com.production.apivalidation.dto.Response;
import com.production.apivalidation.service.ValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpServerErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
Class : ValidationController
Type : Controller
Desc : Controller class with default path 'api'
 */

@Controller
@RequestMapping("api")
public class ValidationController
{

    @Autowired
    private ValidationService validationService;

    Logger logger=LoggerFactory.getLogger(ValidationController.class);

    @GetMapping({"","/"})
    public String home(Model model)
    {
        logger.info("Entry into home path 'api'");
        List<String> projects=this.validationService.getAllProjectName();
        model.addAttribute("selectedProject", projects.isEmpty()?"NOT AVAILABLE":projects.get(0));
        model.addAttribute("isError",false);
        model.addAttribute("isWarning",false);
        model.addAttribute("projects",projects);
        model.addAttribute("errors",new ArrayList<>());
        model.addAttribute("warning",new ArrayList<>());
        model.addAttribute("success",new ArrayList<>());
        logger.info("Exit from home path 'api'");

        return "index";
    }


    @GetMapping("validate/{type}/{project}")
    public String validate(@PathVariable String type,@PathVariable String project, Model model)
    {
        logger.info("Entry into validate {} routes of {} project",type,project);
        List<Response> responses=this.validationService.validate(type,project);
        List<String> projects=this.validationService.getAllProjectName();
        model.addAttribute("selectedProject",project);
        model.addAttribute("isError",responses.stream().anyMatch(response -> response.getStatus() >= 400 && response.getStatus()<=599));
        model.addAttribute("isWarning",responses.stream().anyMatch(response -> response.getStatus() == 0));
        model.addAttribute("projects",projects);
        model.addAttribute("warning",responses.stream().filter(response -> response.getStatus() == 0).collect(Collectors.toList()));
        model.addAttribute("errors",responses.stream().filter(response -> response.getStatus() >= 300 && response.getStatus()<=599).collect(Collectors.toList()));
        model.addAttribute("success",responses.stream().filter(response -> response.getStatus() >= 100 && response.getStatus()<=299).collect(Collectors.toList()));
        logger.info("Exit from validate {} routes of {} project",type,project);
        return "index";
    }
}
