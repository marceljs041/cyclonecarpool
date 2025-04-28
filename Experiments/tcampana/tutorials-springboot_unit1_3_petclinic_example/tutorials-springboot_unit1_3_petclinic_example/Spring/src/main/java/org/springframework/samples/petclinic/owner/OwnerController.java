/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @Modified By Tanmay Ghosh
 * @Modified By Vivek Bengre
 * @Modified By Anthony Campana
 */
@RestController
class OwnerController {

    @Autowired
    OwnerRepository ownersRepository;

    private final Logger logger = LoggerFactory.getLogger(OwnerController.class);

    @RequestMapping(method = RequestMethod.POST, path = "/owners/new")
    public String saveOwner(Owners owner) {
        ownersRepository.save(owner);
        return "New Owner with ID: "+ owner.getId() + " has been added as an admin to Cyclone Carpool.";
    }
     // function to create owner data
    @RequestMapping(method = RequestMethod.GET, path = "/owners/create")
    public String createOwnerData() {
        Owners o1 = new Owners(1, "Anthony", "Campana", "tcampana@iastate.edu", "224-828-8681");
        Owners o2 = new Owners(2, "Marcel", "Slowikowski", "marceljs@iastate.edu", "331-643-7534");
        Owners o3 = new Owners(3, "Tyler", "Gorton", "tjgorton@iastate.edu", "515-500-7072");
        Owners o4 = new Owners(4, "Eddie", "Gong", "egong@iastate.edu", "319-899-3967");
        ownersRepository.save(o1);
        ownersRepository.save(o2);
        ownersRepository.save(o3);
        ownersRepository.save(o4);
        return "Successfully created owner data for Cyclone Carpool, team 2-Jabir-5.";
    }

    @RequestMapping(method = RequestMethod.GET, path = "/owners")
    public List<Owners> getAllOwners() {
        logger.info("Entered into Controller Layer");
        List<Owners> results = ownersRepository.findAll();
        logger.info("Number of Records Fetched:" + results.size());
        return results;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/owners/{ownerId}")
    public Optional<Owners> findOwnerById(@PathVariable("ownerId") int id) {
        logger.info("Entered into Controller Layer");
        Optional<Owners> results = ownersRepository.findById(id);
        return results;
    }

    // PUT method to update an owner's data
    @RequestMapping(method = RequestMethod.PUT, path = "/owners/{ownerId}")
    public String updateOwner(@PathVariable("ownerId") int id, @RequestBody Owners updatedOwner) {
        Optional<Owners> existingOwner = ownersRepository.findById(id);
        if (existingOwner.isPresent()) {
            Owners owner = existingOwner.get();
            owner.setFirstName(updatedOwner.getFirstName());
            owner.setLastName(updatedOwner.getLastName());
            owner.setEmail(updatedOwner.getEmail());
            owner.setTelephone(updatedOwner.getTelephone());
            ownersRepository.save(owner);
            return "Owner " + owner.getFirstName() + " " + owner.getLastName() + " has been updated.";
        } else {
            return "Owner with ID " + id + " not found.";
        }
    }

    // DELETE method to delete an owner by ID
    @RequestMapping(method = RequestMethod.DELETE, path = "/owners/{ownerId}")
    public String deleteOwner(@PathVariable("ownerId") int id) {
        Optional<Owners> existingOwner = ownersRepository.findById(id);
        if (existingOwner.isPresent()) {
            Owners owner = existingOwner.get();
            String first = owner.getFirstName();
            String last = owner.getLastName();
            ownersRepository.deleteById(id);
            return "Owner with ID " + id + " " + first + " " + last + "'s account has been deleted.";
        } else {
            return "Owner with ID " + id + " not found.";
        }
    }

}
