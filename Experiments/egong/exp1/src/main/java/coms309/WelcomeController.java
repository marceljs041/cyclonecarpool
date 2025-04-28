package coms309;

import org.springframework.web.bind.annotation.*;

/**
 * Simple Greeter that processes input Names
 *
 * @author Eddie M.
 */
@RestController
class WelcomeController {

    // This start-up URL asks for user's name, sends to server.
    @GetMapping("/")
    public String welcomeForm() {
        return "<form action='/hello/' method='POST' onsubmit=\"this.action += document.getElementById('name').value\">" +
                "  Hello, what's your name?<br/> <input type='text' id='name' name='name' />" +
                "  <button type='submit'>Submit</button>" +
                "</form>";
    }

    // Based on the name, sends user to their individual greet URLs.
    @PostMapping("/hello/{name}")
    public String welcome(@PathVariable String name) {
        return "Welcome to my COM S 309 Exp1, " + name;
    }
}
