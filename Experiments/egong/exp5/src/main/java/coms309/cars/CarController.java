package coms309.cars;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;


import java.util.HashMap;

/**
 * Controller used to manage Car data, experiment extended from PeopleController
 *
 * @author Eddie M.
 */

@RestController
public class CarController {

    HashMap<String, Car> carList = new  HashMap<>();

    //CRUDL (create/read/update/delete/list)
    // use POST, GET, PUT, DELETE, GET methods for CRUDL

    // THIS IS THE LIST OPERATION
    // gets all the people in the list and returns it in JSON format
    // This controller takes no input.
    // Springboot automatically converts the list to JSON format
    // in this case because of @ResponseBody
    // Note: To LIST, we use the GET method
    @GetMapping("/cars")
    public  HashMap<String,Car> getAllCars() {
        return carList;
    }

    // THIS IS THE CREATE OPERATION
    @PostMapping("/cars")
    public  String addCar(@RequestBody Car car) {
        System.out.println(car);
        carList.put(car.getOwner(), car);
        return car.getOwner() + "'s car Saved";
    }

    // THIS IS THE READ OPERATION
    @GetMapping("/cars/{owner}")
    public Car getCar(@PathVariable String owner) {
        Car c = carList.get(owner);
        return c;
    }

    // THIS IS THE UPDATE OPERATION
    @PutMapping("/cars/{owner}")
    public Car updateCar(@PathVariable String owner, @RequestBody Car c) {
        carList.replace(owner, c);
        return carList.get(owner);
    }

    // THIS IS THE DELETE OPERATION
    @DeleteMapping("/cars/{owner}")
    public HashMap<String, Car> deleteCar(@PathVariable String owner) {
        carList.remove(owner);
        return carList;
    }
}

