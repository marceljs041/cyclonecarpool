package coms309.people;


/**
 * Provides the Definition/Structure for the people row
 *
 * @author Vivek Bengre
 * @Modified By Anthony Campana
 */

public class Person {

    private String firstName;

    private String lastName;

    private Integer age;

    private Integer height;

    private Integer weight;

    private String gender;

    private String address;

    private String telephone;

    public Person(){
        
    }

    public Person(String firstName, String lastName, Integer age, Integer height, Integer weight, String gender, String address, String telephone){
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
        this.address = address;
        this.telephone = telephone;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() { return this.age; }

    public void setAge(Integer age) { this.age = age; }

    public int getHeight() { return this.height; }

    public void setHeight(Integer height) { this.height = height; }

    public int getWeight() { return this.weight; }

    public void setWeight(Integer weight) { this.weight = weight; }

    public String getGender() { return this.gender; }

    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @Override
    public String toString() {
        return firstName + " " 
               + lastName + " "
               + age + " "
               + height + " "
               + weight + " "
               + gender + " "
               + address + " "
               + telephone;
    }
}
