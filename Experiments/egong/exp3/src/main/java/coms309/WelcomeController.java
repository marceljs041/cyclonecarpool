package coms309;

import org.springframework.web.bind.annotation.*;

/**
 * Simple Hello World Controller to display the string returned
 *
 * @author Vivek Bengre
 */

@RestController
class WelcomeController {

    // This start-up URL asks for user's name, sends to server.
    @GetMapping("/")
    public String welcomeForm() {
        return "<form action='/hello/' method='POST' onsubmit=\"this.action += document.getElementById('name').value\">" +
                "  Hello, what's your name?<br/> <input type='text' id='name' name='name' /><br/>" +
                "  <button type='submit'>Submit</button>" +
                "</form>";
    }

    // Based on the name, sends user to their individual greet URLs(or not "Go Cyclones!").
    // Attempting a map through an open-source JavaScript library (sample route: ISU -> DSM)
    @PostMapping("/hello/{name}")
    public String welcome(@RequestParam String name) throws RuntimeException {
        if ("Hawkeye".equalsIgnoreCase(name))throw new RuntimeException("Hawkeyes are not welcomed!");

        return "Welcome to my COM S 309 Exp3, " + name + "<br/><br/>" +
                "Here's a sample route from Iowa State University to Des Moines International Airport<br/>"+

                "<div id='map' style='height: 500px; width: 100%;'></div>" +
                "<script src='https://unpkg.com/leaflet/dist/leaflet.js'></script>" +
                "<link rel='stylesheet' href='https://unpkg.com/leaflet/dist/leaflet.css' />" +
                "<script>" +
                "var map = L.map('map').setView([42.0308, -93.6319], 9);" +
                "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "   attribution: 'Map data &copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'," +
                "   maxZoom: 19" +
                "}).addTo(map);" +

                "L.marker([42.0261, -93.6480]).addTo(map).bindPopup('Iowa State University');" +
                "L.marker([41.5341, -93.6631]).addTo(map).bindPopup('Des Moines International Airport');" +

                "var route = [[42.0261, -93.6480], [41.5341, -93.6631]];" +
                "var polyline = L.polyline(route, {color: 'blue'}).addTo(map);" +
                "</script>";
    }
}
