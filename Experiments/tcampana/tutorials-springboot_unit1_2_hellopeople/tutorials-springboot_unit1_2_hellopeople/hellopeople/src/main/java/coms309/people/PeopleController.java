package coms309.people;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.HashMap;

/**
 * Controller used to showcase Create and Read from a LIST
 *
 * @author Vivek Bengre
 * @Modified by Anthony Campana
 */

@RestController
public class PeopleController {

    // Note that there is only ONE instance of PeopleController in 
    // Springboot system.
    HashMap<String, Person> peopleList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input. 
    // Springboot automatically converts the list to JSON format 
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/people")
    public  HashMap<String,Person> getAllPersons() {
        return peopleList;
    }

    // THIS IS THE CREATE OPERATION
    // springboot automatically converts JSON input into a person object and 
    // the method below enters it into the list.
    // It returns a string message in THIS example.
    // in this case because of @ResponseBody
    // Note: To CREATE we use POST method
    @PostMapping("/people")
    public  String createPerson(@RequestBody Person person) {
        System.out.println(person);
        peopleList.put(person.getFirstName(), person);
        return "The user " + person.getFirstName() + " " + person.getLastName() + " Has Been Created Successfully!";
    }

    // THIS IS THE READ OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We extract the person from the HashMap.
    // springboot automatically converts Person to JSON format when we return it
    // in this case because of @ResponseBody
    // Note: To READ we use GET method
    @GetMapping("/people/{firstName}")
    public Person getPerson(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p;
    }

    @GetMapping("/people/{firstName}/lastName")
    public String getLastName(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + "'s last name is " + p.getLastName() + "!";
    }

    @GetMapping("/people/{firstName}/age")
    public String getAge(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + " is " + p.getAge() + " years old!";
    }

    @GetMapping("/people/{firstName}/weight")
    public String getWeight(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + " is " + p.getWeight() + " lbs!";
    }

    @GetMapping("/people/{firstName}/height")
    public String getHeight(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + " is " + p.getHeight() + " inches tall!";
    }

    @GetMapping("/people/{firstName}/gender")
    public String getGender(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + " is " + p.getGender() + "!";
    }

    @GetMapping("/people/{firstName}/address")
    public String getAddress(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + " has the address, " + p.getAddress() + ".";
    }

    @GetMapping("/people/{firstName}/telephone")
    public String getTelephone(@PathVariable String firstName) {
        Person p = peopleList.get(firstName);
        return p.getFirstName() + " " + p.getLastName() + "'s phone number is " + p.getTelephone() + ".";
    }

    // THIS IS THE UPDATE OPERATION
    // We extract the person from the HashMap and modify it.
    // Springboot automatically converts the Person to JSON format
    // Springboot gets the PATHVARIABLE from the URL
    // Here we are returning what we sent to the method
    // in this case because of @ResponseBody
    // Note: To UPDATE we use PUT method
    @PutMapping("/people/{firstName}")
    public Person updatePerson(@PathVariable String firstName, @RequestBody Person p) {
        peopleList.replace(firstName, p);
        return peopleList.get(firstName);
    }


    // THIS IS THE DELETE OPERATION
    // Springboot gets the PATHVARIABLE from the URL
    // We return the entire list -- converted to JSON
    // in this case because of @ResponseBody
    // Note: To DELETE we use delete method
    
    @DeleteMapping("/people/{firstName}")
    public HashMap<String, Person> deletePerson(@PathVariable String firstName) {
        peopleList.remove(firstName);
        return peopleList;
    }

}

