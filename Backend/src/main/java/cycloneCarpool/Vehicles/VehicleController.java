package cycloneCarpool.Vehicles;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author eddiegong
 */

@Tag(name = "Vehicle Controller",
        description = "Operations Related to Vehicles, Permission Check.")
@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    // expecting 1 vehicle max that is tied to a client account

    @Autowired
    VehicleService vehicleService;

    @Operation(summary = "Fetch All Vehicles.",
            description = "Returns List of All Vehicles in the Repository, Sorted by ID, Returns " +
                    "[200 + List].")
    @GetMapping("/all")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    @Operation(summary = "Given Vehicle Information, Create a New Vehicle Entry.",
            description = "Create New Vehicle Entry, Confirm Owner Exists, Confirm Entry with Owner Does Not Exist, " +
                    "Fetch Information by License Plate and State, Estimate MPG, Save to Repository, Returns " +
                    "[200 + Entry] if Successful, " +
                    "[404] if No Such User(Owner) or License Plate Info Found in Corresponding Repository, " +
                    "[403] if Owner Registered Another Vehicle, " +
                    "[500] if Cars API Unable to Provide MPG.")
    @PostMapping("/create")
    public ResponseEntity<Vehicle> createVehicle(@RequestBody Vehicle vehicle) {
        try {
            Vehicle newVehicle = vehicleService.createVehicle(vehicle);
            return ResponseEntity.status(HttpStatus.CREATED).body(newVehicle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    @Operation(summary = "Given Vehicle ID, Fetch Vehicle.",
            description = "Search Repository for an Entry of Given Vehicle ID, Returns " +
                    "[200 + Entry] if Successful," +
                    "[404] if No Such Vehicle Found in Repository.")
    @GetMapping("{vehicleId}")
    public ResponseEntity<Vehicle> getVehicle(@PathVariable Long vehicleId) {
        try {
            Vehicle vehicle = vehicleService.getVehicleById(vehicleId);
            return ResponseEntity.ok(vehicle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Given Owner ID, Fetch Vehicle.",
            description = "Search Repository for an Entry of Given Owner ID, Returns " +
                    "[200 + Entry] if Successful," +
                    "[404] if No Such Vehicle Found in Repository.")
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<Vehicle> getVehiclesByOwner(@PathVariable Long ownerId) {
        try {
            Vehicle vehicle = vehicleService.getVehicleByOwnerId(ownerId);
            return ResponseEntity.ok(vehicle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Given Vehicle ID and Owner ID, Check if User has Permission to Edit the Vehicle.",
            description = "Check if the User has Edit Permission of the Vehicle, " +
                    "Client has Edit Permission if They are the Owner of the Vehicle, Returns " +
                    "[200] if Client has Edit Permission of the Vehicle," +
                    "[403] if Not.")
    @PostMapping("{vehicleId}/edit")
    public ResponseEntity<Void>checkVehicleEditPermission(@PathVariable Long vehicleId, @RequestBody Vehicle ownerId) {
        boolean permission = vehicleService.checkVehicleEditPermission(vehicleId, ownerId.getOwnerId());

        if (permission) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Given Vehicle ID and New Vehicle Content, Update Vehicle Entry in Repository.",
            description = "Update Repository(Only License Plate and State) an Entry of Given Vehicle ID, Returns " +
                    "[200 + Entry(After Change)] if Successful, " +
                    "[404] if No Such Vehicle or Updated License Plate Info Found in Corresponding Repository," +
                    "[500] if Cars API Unable to Provide New MPG.")
    @PutMapping("{vehicleId}/edit")
    public ResponseEntity<?> editVehicle(@PathVariable Long vehicleId, @RequestBody Vehicle vehicle) {
        try {
            Vehicle editedVehicle = vehicleService.editVehicle(vehicleId, vehicle);
            return ResponseEntity.ok(editedVehicle);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Given Vehicle ID, Remove Vehicle Entry from Repository.",
            description = "Remove Repository an Entry of Given Vehicle ID, Returns " +
                    "[204] if Successful, " +
                    "[404] if No Such Vehicle Found.")
    @DeleteMapping("{vehicleId}")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long vehicleId) {
        boolean deleted = vehicleService.deleteVehicleById(vehicleId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Given Owner ID, Remove Vehicle Entry from Repository.",
            description = "Remove Repository an Entry of Given Owner ID, Returns " +
                    "[204] if Successful, " +
                    "[404] if No Such Vehicle Found.")
    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<Void> deleteVehicleByOwner(@PathVariable Long ownerId) {
        boolean deleted = vehicleService.deleteVehicleByOwnerId(ownerId);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
}
