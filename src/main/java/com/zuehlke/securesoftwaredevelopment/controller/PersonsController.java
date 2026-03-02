package com.zuehlke.securesoftwaredevelopment.controller;

import com.zuehlke.securesoftwaredevelopment.config.AuditLogger;
import com.zuehlke.securesoftwaredevelopment.config.SecurityUtil;
import com.zuehlke.securesoftwaredevelopment.domain.Person;
import com.zuehlke.securesoftwaredevelopment.domain.User;
import com.zuehlke.securesoftwaredevelopment.repository.PersonRepository;
import com.zuehlke.securesoftwaredevelopment.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@Controller

public class PersonsController {

    private static final Logger LOG = LoggerFactory.getLogger(PersonsController.class);
    private static final AuditLogger auditLogger = AuditLogger.getAuditLogger(PersonRepository.class);

    private final PersonRepository personRepository;
    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    public PersonsController(PersonRepository personRepository, UserRepository userRepository,SecurityUtil securityUtil) {
        this.personRepository = personRepository;
        this.userRepository = userRepository;
        this.securityUtil=securityUtil;
    }

    @GetMapping("/persons/{id}")
    public String person(@PathVariable int id, Model model) {
        model.addAttribute("person", personRepository.get("" + id));
        model.addAttribute("username", userRepository.findUsername(id));
        return "person";
    }

    @GetMapping("/myprofile")
    public String self(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        model.addAttribute("person", personRepository.get("" + user.getId()));
        model.addAttribute("username", userRepository.findUsername(user.getId()));
        return "person";
    }

    @DeleteMapping("/persons/{id}")
    public ResponseEntity<Void> person(@PathVariable int id) {
        personRepository.delete(id);
        userRepository.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/update-person")
    @PreAuthorize("hasAuthority('UPDATE_PERSON')")
    public String updatePerson(Person person, String username) {
            if((!securityUtil.hasPermission(username))) {
                throw new AccessDeniedException("You are not allowed to update this person");
            }
        personRepository.update(person);
        userRepository.updateUsername(Integer.parseInt(person.getId()), username);
        return "redirect:/persons/" + person.getId();
    }

    @GetMapping("/persons")
    public String persons(Model model) {
        model.addAttribute("persons", personRepository.getAll());
        return "persons";
    }

    @GetMapping(value = "/persons/search", produces = "application/json")
    @ResponseBody
    public List<Person> searchPersons(@RequestParam String searchTerm) throws SQLException {
        return personRepository.search(searchTerm);
    }
}
