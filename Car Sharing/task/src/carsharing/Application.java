package carsharing;

import carsharing.data.Car;
import carsharing.data.Company;
import carsharing.data.Customer;
import carsharing.data.dao.CarDaoImpl;
import carsharing.data.dao.CompanyDaoImpl;
import carsharing.data.dao.CustomerDaoImpl;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class Application {
    private static final String BACK_MENU = "0. Back\n";
    private static final String CAR_LIST_EMPTY = "The car list is empty!\n";

    private static final Scanner scanner = new Scanner(System.in);
    private static CompanyDaoImpl companyDao;
    private static CarDaoImpl carDao;
    private static CustomerDaoImpl customerDao;

    private Application() {
    }

    public static void printMainMenu(Connection connection) {
        companyDao = new CompanyDaoImpl(connection, "COMPANY");
        carDao = new CarDaoImpl(connection, "CAR");
        customerDao = new CustomerDaoImpl(connection, "CUSTOMER");

        boolean exit = false;
        while (!exit) {
            System.out.println("""
                    1. Log in as a manager
                    2. Log in as a customer
                    3. Create a customer
                    0. Exit
                    """);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> printManagerSubMenu();
                case "2" -> printCustomerSubMenu();
                case "3" -> {
                    System.out.println("Enter the customer name:");
                    customerDao.save(new Customer(scanner.nextLine()));
                    System.out.println("The customer was created!\n");
                }
                case "0" -> exit = true;
                default -> System.out.println("Such value is not supported");
            }
        }
    }

    private static void printCarCompanyMenu(Company company) throws InterruptedException {
        boolean back = false;
        while (!back) {
            System.out.printf("""
                    '%s' company:
                    1. Car list
                    2. Create a car
                    0. Back
                    %n""", company.getName());
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> {
                    List<Car> cars = carDao.getByCompanyId(company.getId());
                    if (cars.size() > 0) {
                        System.out.println("Car list:");
                        for (int i = 0; i < cars.size(); i++) {
                            System.out.println((i + 1) + ". " + cars.get(i).getName() + " rented: " + customerDao.isCarLocked(cars.get(i).getId()));
                        }
                    } else {
                        System.out.println(CAR_LIST_EMPTY);
                    }
                }
                case "2" -> {
                    System.out.println("Enter the car name:");
                    String car_name = scanner.nextLine();
                    carDao.save(new Car(car_name, company.getId()));
                    System.out.println("The car was created!\n");
                }
                case "0" -> throw new InterruptedException("Exit to main menu");
                default -> System.out.println("Such value is not supported");
            }
        }
    }

    private static void printCustomerChoiceMenu(Customer customer) throws InterruptedException {
        boolean back = false;
        while (!back) {
            System.out.println("""
                    1. Rent a car
                    2. Return a rented car
                    3. My rented car
                    0. Back
                    """);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> { // Rent a car
                    if (customerDao.getRentedCar(customer.getId()).isPresent()) {
                        System.out.println("You've already rented a car!\n");
                        break;
                    }
                    List<Company> companies = companyDao.getAll();
                    if (companies.size() > 0) {
                        System.out.println("Choose a company:");
                        for (int i = 0; i < companies.size(); i++) {
                            System.out.println((i + 1) + ". " + companies.get(i).getName());
                        }
                        System.out.println(BACK_MENU);
                        String choiceCompany = scanner.nextLine();
                        if (Integer.parseInt(choiceCompany) == 0) {
                            back = true;
                        } else {
                            Integer companyId = companies.get(Integer.parseInt(choiceCompany) - 1).getId();
                            List<Car> cars = carDao.getByCompanyId(companyId)
                                    .stream()
                                    .filter(c -> !customerDao.isCarLocked(c.getId()))
                                    .toList();
                            if (cars.size() > 0) {
                                System.out.println("Choose a car:");
                                for (int i = 0; i < cars.size(); i++) {
                                    System.out.println((i + 1) + ". " + cars.get(i).getName());
                                }
                                int index = scanner.nextInt();
                                index = index <= 0 ? 1 : index;
                                Integer carId = cars.get(index - 1).getId();
                                String[] params = new String[]{Integer.toString(carId)};
                                customerDao.update(customer, params);
                                System.out.println("You rented '" + cars.get(index - 1).getName() + "'");
                            } else {
                                System.out.println(CAR_LIST_EMPTY);
                            }
                        }
                    } else {
                        System.out.println("The company list is empty!\n");
                        back = true;
                    }
                }
                case "2" -> { // Return a rented car
                    Optional<Car> rentedCar = customerDao.getRentedCar(customer.getId());
                    if (rentedCar.isPresent()) {
                        customerDao.releaseCar(customer.getId());
                        System.out.println("You've returned a rented car! " + rentedCar.get().getName() + "\n");
                    } else {
                        System.out.println("You didn't rent a car!\n");
                    }
                }
                case "3" -> { // My rented car
                    Optional<Car> myCar = customerDao.getRentedCar(customer.getId());
                    if (myCar.isPresent()) {
                        System.out.println("Your rented car:");
                        System.out.println(myCar.get().getName());
                        System.out.println("Company:");
                        System.out.println(companyDao.get(myCar.get().getCompanyId()).get().getName());
                    } else {
                        System.out.println("You didn't rent a car!\n");
                    }
                }
                case "0" -> throw new InterruptedException("Exit to main menu\n");
                default -> System.out.println("Such value is not supported");
            }
        }
    }

    private static void printCompanyChooseMenu() {
        boolean back = false;
        while (!back) {
            List<Company> companies = companyDao.getAll();
            if (companies.size() > 0) {
                System.out.println("Choose the company:");
                for (int i = 0; i < companies.size(); i++) {
                    System.out.println((i + 1) + ". " + companies.get(i).getName());
                }
                System.out.println(BACK_MENU);
                String choice = scanner.nextLine();
                if (Integer.parseInt(choice) == 0) {
                    back = true;
                } else {
                    try {
                        printCarCompanyMenu(companies.get(Integer.parseInt(choice) - 1));
                    } catch (InterruptedException e) {
                        back = true;
                    }
                }
            } else {
                System.out.println("The company list is empty!\n");
                back = true;
            }
        }
    }

    private static void printCustomerSubMenu() {
        boolean back = false;
        while (!back) {
            List<Customer> customers = customerDao.getAll();
            if (customers.size() > 0) {
                System.out.println("Choose a customer:");
                for (int i = 0; i < customers.size(); i++) {
                    System.out.println((i + 1) + ". " + customers.get(i).getName());
                }
                System.out.println(BACK_MENU);
                String choice = scanner.nextLine();
                if (Integer.parseInt(choice) == 0) {
                    back = true;
                } else {
                    try {
                        printCustomerChoiceMenu(customers.get(Integer.parseInt(choice) - 1));
                    } catch (InterruptedException e) {
                        back = true;
                    }
                }
            } else {
                System.out.println("The customer list is empty!\n");
                back = true;
            }
        }
    }

    private static void printManagerSubMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("""
                    1. Company list
                    2. Create a company
                    0. Back
                    """);
            String choice = scanner.nextLine();
            switch (choice) {
                case "1" -> printCompanyChooseMenu();
                case "2" -> {
                    System.out.println("Enter the company name:");
                    String companyName = scanner.nextLine();
                    companyDao.save(new Company(companyName));
                    System.out.println("The company was created!\n");
                }
                case "0" -> back = true;
                default -> System.out.println("Such value is not supported");
            }
        }
    }
}
